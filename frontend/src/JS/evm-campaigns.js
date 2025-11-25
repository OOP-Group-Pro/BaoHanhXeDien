// src/JS/evm-campaigns.js

// 1. IMPORT CÁC SERVICE
import {
    searchCampaigns,
    createCampaign,
    addAffectedVehicle,
    updateCampaign,
    getCampaignDetail,
    deleteCampaign,        // <-- Đã thêm
    searchAffectedVehicles // <-- Đã thêm
} from '../services/campaignService.js';

import { searchVehicles } from '../services/vehicleService.js';

// 2. KHAI BÁO STATE
let state = {
    page: 0,
    size: 10,
    code: '',
    status: ''
};

let vehicleSearchState = { keyword: '', page: 0, totalPages: 0 };
let selectedVins = new Set();

// 3. KHAI BÁO MODAL
let createModal = null;
let editModal = null;
let vehicleSelectModal = null;
let viewModal = null;

/**
 * HÀM KHỞI TẠO CHÍNH (Gọi từ evmStaff.js)
 */
export function setupCampaignManagement() {
    console.log("LOG: Init Campaign Management JS...");

    // Khởi tạo các Modal
    const createEl = document.getElementById('createCampaignModal');
    if (createEl) createModal = new bootstrap.Modal(createEl);

    const editEl = document.getElementById('editCampaignModal');
    if (editEl) editModal = new bootstrap.Modal(editEl);

    const viewEl = document.getElementById('viewCampaignModal');
    if (viewEl) viewModal = new bootstrap.Modal(viewEl);

    const vehicleEl = document.getElementById('vehicleSelectModal');
    if (vehicleEl) vehicleSelectModal = new bootstrap.Modal(vehicleEl);

    // --- GÁN SỰ KIỆN (EVENT LISTENERS) ---

    // 1. Bộ lọc và tạo mới
    document.getElementById('btnFilter').addEventListener('click', handleFilter);
    document.getElementById('btnSaveCampaign').addEventListener('click', handleCreateCampaign);
    document.getElementById('btnSaveEdit').addEventListener('click', handleSaveEdit);

    // 2. Modal Chọn xe
    document.getElementById('btnOpenVehicleModal').addEventListener('click', () => {
        vehicleSelectModal.show();
        // Reset và tải danh sách xe ngay
        document.getElementById('vehicleSearchKeyword').value = '';
        vehicleSearchState.keyword = '';
        vehicleSearchState.page = 0;
        fetchAndRenderVehicleModal();
        updateModalSelectedCount();
    });

    document.getElementById('btnSearchVehicle').addEventListener('click', handleSearchVehicleModal);
    document.getElementById('btnConfirmSelection').addEventListener('click', confirmVehicleSelection);

    // 3. Xóa tag VIN
    document.getElementById('selectedVinContainer').addEventListener('click', (e) => {
        if (e.target.closest('.btn-remove-vin')) {
            const vinToRemove = e.target.closest('.btn-remove-vin').dataset.vin;
            selectedVins.delete(vinToRemove);
            renderSelectedVinTags();
        }
    });

    // TẢI DỮ LIỆU BAN ĐẦU
    fetchAndRenderCampaigns();
}

// ============================================================
// PHẦN 1: QUẢN LÝ DANH SÁCH CHIẾN DỊCH (Table)
// ============================================================

async function handleFilter() {
    state.code = document.getElementById('filterCode').value.trim();
    state.status = document.getElementById('filterStatus').value;
    state.page = 0;
    await fetchAndRenderCampaigns();
}

async function fetchAndRenderCampaigns() {
    const tbody = document.getElementById('campaignTbody');
    tbody.innerHTML = '<tr><td colspan="6" class="text-center"><div class="spinner-border text-success"></div></td></tr>';

    try {
        const params = {
            page: state.page,
            size: state.size
        };
        if (state.code) params.code = state.code;
        if (state.status) params.status = state.status;

        const data = await searchCampaigns(params);
        renderTable(data.content);
        renderPagination(data);

    } catch (error) {
        console.error("Lỗi tải campaign:", error);
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-danger">Lỗi: ${error.message}</td></tr>`;
    }
}

function renderTable(campaigns) {
    const tbody = document.getElementById('campaignTbody');
    tbody.innerHTML = '';

    if (!campaigns || campaigns.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">Chưa có chiến dịch nào.</td></tr>';
        return;
    }

    campaigns.forEach(camp => {
        const start = new Date(camp.startAt).toLocaleDateString('vi-VN');
        const end = new Date(camp.endAt).toLocaleDateString('vi-VN');

        let statusBadge = 'bg-secondary';
        if (camp.status === 'ACTIVE') statusBadge = 'bg-success';
        if (camp.status === 'DRAFT') statusBadge = 'bg-warning text-dark';
        if (camp.status === 'CLOSED') statusBadge = 'bg-danger';

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="ps-4 fw-bold font-monospace text-primary">${camp.code}</td>
            <td>${camp.title}</td>
            <td><span class="badge bg-info text-dark">${camp.type}</span></td>
            <td>${start} - ${end}</td>
            <td><span class="badge ${statusBadge}">${camp.status}</span></td>
            <td class="text-end pe-4">
                <button class="btn btn-sm btn-outline-warning me-1 btn-edit"
                        data-id="${camp.id}" title="Cập nhật">
                    <i class="bi bi-pencil-square"></i>
                </button>

                <button class="btn btn-sm btn-outline-primary me-1 btn-view"
                        data-id="${camp.id}" title="Xem chi tiết">
                    <i class="bi bi-eye"></i>
                </button>

                <button class="btn btn-sm btn-outline-danger btn-delete"
                        data-id="${camp.id}" title="Xóa">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });

    // Gắn sự kiện nút bấm
    document.querySelectorAll('.btn-edit').forEach(btn => {
        btn.addEventListener('click', (e) => openEditModal(e.currentTarget.dataset.id));
    });
    document.querySelectorAll('.btn-view').forEach(btn => {
        btn.addEventListener('click', (e) => openViewModal(e.currentTarget.dataset.id));
    });
    document.querySelectorAll('.btn-delete').forEach(btn => {
        btn.addEventListener('click', (e) => handleDeleteCampaign(e.currentTarget.dataset.id));
    });
}

function renderPagination(pageData) {
    const paginationUl = document.getElementById('pagination');
    paginationUl.innerHTML = '';
    const { totalPages, number: currentPage } = pageData;
    if (totalPages <= 1) return;

    paginationUl.innerHTML = `
        <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
            <button class="page-link" onclick="window.changePageCamp(${currentPage - 1})">Trước</button>
        </li>
        <li class="page-item disabled"><span class="page-link">${currentPage + 1} / ${totalPages}</span></li>
        <li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}">
            <button class="page-link" onclick="window.changePageCamp(${currentPage + 1})">Sau</button>
        </li>
    `;
}

window.changePageCamp = (page) => {
    state.page = page;
    fetchAndRenderCampaigns();
}

// ============================================================
// PHẦN 2: TẠO MỚI (CREATE) & IMPORT XE
// ============================================================

async function handleCreateCampaign() {
    const btn = document.getElementById('btnSaveCampaign');

    // 1. Lấy dữ liệu Form
    const code = document.getElementById('campCode').value.trim();
    const title = document.getElementById('campTitle').value.trim();
    const type = document.getElementById('campType').value;
    const startAt = document.getElementById('campStart').value;
    const endAt = document.getElementById('campEnd').value;

    // Lấy danh sách VIN từ ô input ẩn (được điền bởi modal chọn xe)
    const vinText = document.getElementById('campVins').value;

    if (!code || !title || !startAt || !endAt) {
        alert("Vui lòng nhập đủ thông tin bắt buộc (*)");
        return;
    }

    const vinList = vinText.split('\n').map(v => v.trim()).filter(v => v !== '');

    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang xử lý...';

    try {
        // B1: Tạo Chiến dịch
        const campDto = { code, title, type, startAt, endAt };
        const newCampaign = await createCampaign(campDto);
        console.log("LOG: Tạo thành công. ID =", newCampaign.id);

        // B2: Import VINs
        if (vinList.length > 0) {
            btn.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Đang import ${vinList.length} xe...`;

            let successCount = 0;
            for (const vin of vinList) {
                try {
                    await addAffectedVehicle(newCampaign.id, {
                        campaignId: newCampaign.id,
                        vehicleVin: vin
                    });
                    successCount++;
                } catch (e) {
                    console.warn(`Lỗi import VIN ${vin}:`, e);
                }
            }
            alert(`Tạo chiến dịch thành công!\nĐã import: ${successCount}/${vinList.length} xe.`);
        } else {
            alert("Tạo chiến dịch thành công (Chưa có xe nào).");
        }

        createModal.hide();
        // Reset form
        document.getElementById('createCampaignForm').reset();
        selectedVins.clear();
        renderSelectedVinTags();

        fetchAndRenderCampaigns();

    } catch (error) {
        alert("Lỗi: " + error.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = 'Lưu & Phát hành';
    }
}

// ============================================================
// PHẦN 3: CẬP NHẬT (EDIT)
// ============================================================

async function openEditModal(id) {
    try {
        const camp = await getCampaignDetail(id);

        document.getElementById('editCampId').value = camp.id;
        document.getElementById('editCampTitle').value = camp.title;
        document.getElementById('editCampType').value = camp.type;
        document.getElementById('editCampStatus').value = camp.status;

        document.getElementById('editCampStart').value = camp.startAt ? camp.startAt.substring(0, 16) : '';
        document.getElementById('editCampEnd').value = camp.endAt ? camp.endAt.substring(0, 16) : '';

        editModal.show();
    } catch (error) {
        alert("Lỗi tải thông tin: " + error.message);
    }
}

async function handleSaveEdit() {
    const id = document.getElementById('editCampId').value;
    const dto = {
        title: document.getElementById('editCampTitle').value,
        type: document.getElementById('editCampType').value,
        status: document.getElementById('editCampStatus').value,
        startAt: document.getElementById('editCampStart').value,
        endAt: document.getElementById('editCampEnd').value
    };

    try {
        await updateCampaign(id, dto);
        alert("Cập nhật thành công!");
        editModal.hide();
        fetchAndRenderCampaigns();
    } catch (error) {
        alert("Lỗi cập nhật: " + error.message);
    }
}

// ============================================================
// PHẦN 4: XEM CHI TIẾT (VIEW)
// ============================================================

async function openViewModal(id) {
    try {
        const camp = await getCampaignDetail(id);

        document.getElementById('viewCode').textContent = camp.code;
        document.getElementById('viewTitle').textContent = camp.title;
        document.getElementById('viewType').textContent = camp.type;
        document.getElementById('viewStatus').textContent = camp.status;
        document.getElementById('viewStart').textContent = new Date(camp.startAt).toLocaleString('vi-VN');
        document.getElementById('viewEnd').textContent = new Date(camp.endAt).toLocaleString('vi-VN');

        loadAffectedVehiclesForView(id);
        viewModal.show();
    } catch (error) {
        alert("Lỗi tải chi tiết: " + error.message);
    }
}

async function loadAffectedVehiclesForView(campaignId) {
    const tbody = document.getElementById('viewVehicleTbody');
    tbody.innerHTML = '<tr><td colspan="3" class="text-center">Đang tải...</td></tr>';

    try {
        const response = await searchAffectedVehicles(campaignId, { size: 100 });
        const vehicles = response.content || [];

        tbody.innerHTML = '';
        if (vehicles.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">Chưa có xe nào.</td></tr>';
            return;
        }

        vehicles.forEach((v, index) => {
            let badge = 'bg-secondary';
            if(v.status === 'NOTIFIED') badge = 'bg-info text-dark';
            if(v.status === 'COMPLETED') badge = 'bg-success';

            tbody.innerHTML += `
                <tr>
                    <td>${index + 1}</td>
                    <td class="font-monospace fw-bold">${v.vehicleVin}</td>
                    <td><span class="badge ${badge}">${v.status}</span></td>
                </tr>
            `;
        });
    } catch (error) {
        console.error(error);
        tbody.innerHTML = '<tr><td colspan="3" class="text-center text-danger">Lỗi tải danh sách xe.</td></tr>';
    }
}

// ============================================================
// PHẦN 5: XÓA (DELETE)
// ============================================================

async function handleDeleteCampaign(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa chiến dịch này không?')) return;

    try {
        await deleteCampaign(id);
        alert('Đã xóa thành công!');
        fetchAndRenderCampaigns();
    } catch (error) {
        alert('Không thể xóa: ' + error.message);
    }
}

// ============================================================
// PHẦN 6: MODAL CHỌN XE (LOGIC MỚI)
// ============================================================

async function handleSearchVehicleModal() {
    const keyword = document.getElementById('vehicleSearchKeyword').value.trim();
    vehicleSearchState.keyword = keyword;
    vehicleSearchState.page = 0;
    await fetchAndRenderVehicleModal();
}

async function fetchAndRenderVehicleModal() {
    const tbody = document.getElementById('vehicleSearchResultBody');
    tbody.innerHTML = '<tr><td colspan="5" class="text-center"><div class="spinner-border spinner-border-sm text-primary"></div></td></tr>';

    try {
        const response = await searchVehicles(vehicleSearchState.keyword, vehicleSearchState.page, 10);
        const pageData = response.data;
        const vehicles = pageData.content || [];
        vehicleSearchState.totalPages = pageData.totalPages;

        renderVehicleModalTable(vehicles);
        renderVehicleModalPagination(pageData);

    } catch (error) {
        console.error("Lỗi tìm xe:", error);
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-danger">Lỗi: ${error.message}</td></tr>`;
    }
}

function renderVehicleModalTable(vehicles) {
    const tbody = document.getElementById('vehicleSearchResultBody');
    tbody.innerHTML = '';

    if (vehicles.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Không tìm thấy xe nào.</td></tr>';
        return;
    }

    vehicles.forEach(v => {
        const isChecked = selectedVins.has(v.vehicleVin) ? 'checked' : '';
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="text-center">
                <input type="checkbox" class="form-check-input vehicle-checkbox"
                       value="${v.vehicleVin}" ${isChecked}
                       onchange="window.toggleVinSelection('${v.vehicleVin}')">
            </td>
            <td class="font-monospace fw-bold">${v.vehicleVin}</td>
            <td>${v.model}</td>
            <td>${v.licensePlate || '-'}</td>
            <td>${v.customer ? v.customer.customerName : 'N/A'}</td>
        `;
        tbody.appendChild(tr);
    });
}

window.toggleVinSelection = (vin) => {
    if (selectedVins.has(vin)) selectedVins.delete(vin);
    else selectedVins.add(vin);
    updateModalSelectedCount();
};

function updateModalSelectedCount() {
    document.getElementById('modalSelectedCount').textContent = `Đang chọn: ${selectedVins.size} xe`;
}

function confirmVehicleSelection() {
    renderSelectedVinTags();
    vehicleSelectModal.hide();
}

function renderSelectedVinTags() {
    const container = document.getElementById('selectedVinContainer');
    const textarea = document.getElementById('campVins');
    const countEl = document.getElementById('vinCount');

    container.innerHTML = '';

    if (selectedVins.size === 0) {
        container.innerHTML = '<span class="text-muted small fst-italic p-1">Chưa có xe nào được chọn.</span>';
        textarea.value = '';
        countEl.textContent = 'Đã chọn: 0 xe';
        return;
    }

    selectedVins.forEach(vin => {
        const tag = document.createElement('span');
        tag.className = "badge bg-primary me-1 mb-1 pe-1";
        tag.innerHTML = `
            ${vin}
            <button type="button" class="btn-close btn-close-white ms-2 btn-remove-vin"
                    style="font-size: 0.5em;" aria-label="Close" data-vin="${vin}"></button>
        `;
        container.appendChild(tag);
    });

    textarea.value = Array.from(selectedVins).join('\n');
    countEl.textContent = `Đã chọn: ${selectedVins.size} xe`;
}

function renderVehicleModalPagination(pageData) {
    const paginationUl = document.getElementById('vehicleModalPagination');
    paginationUl.innerHTML = '';
    const { totalPages, number: currentPage } = pageData;
    if (totalPages <= 1) return;

    paginationUl.innerHTML = `
        <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
            <button class="page-link" onclick="window.changeVehicleModalPage(${currentPage - 1})">Trước</button>
        </li>
        <li class="page-item disabled"><span class="page-link">${currentPage + 1} / ${totalPages}</span></li>
        <li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}">
            <button class="page-link" onclick="window.changeVehicleModalPage(${currentPage + 1})">Sau</button>
        </li>
    `;
}

window.changeVehicleModalPage = (page) => {
    vehicleSearchState.page = page;
    fetchAndRenderVehicleModal();
}