// src/JS/evm-campaigns.js

// 1. IMPORT CÁC SERVICE
import {
    searchCampaigns,
    createCampaign,
    addAffectedVehicle,
    updateCampaign,
    getCampaignDetail,
    deleteCampaign,
    searchAffectedVehicles
} from '../services/campaignService.js';

import { searchVehicles } from '../services/vehicleService.js';
import { searchParts } from '../services/partService.js'; // <--- MỚI

// 2. KHAI BÁO STATE
let state = {
    page: 0,
    size: 10,
    code: '',
    status: ''
};

let vehicleSearchState = { keyword: '', page: 0, totalPages: 0 };
let selectedVins = new Set();
let selectedParts = []; // <--- MỚI: Lưu danh sách phụ tùng đã chọn
// 1. Thêm biến lưu state tìm kiếm cho Part
let partSearchState = { keyword: '', page: 0 };

// 3. KHAI BÁO MODAL
let createModal = null;
let editModal = null;
let vehicleSelectModal = null;
let viewModal = null;
let partSelectModal = null; // <--- MỚI

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

    const partEl = document.getElementById('partSelectModal'); // <--- MỚI
    if (partEl) partSelectModal = new bootstrap.Modal(partEl);

    // --- GÁN SỰ KIỆN (EVENT LISTENERS) ---

    // 1. Bộ lọc và tạo mới
    document.getElementById('btnFilter').addEventListener('click', handleFilter);
    document.getElementById('btnSaveCampaign').addEventListener('click', handleCreateCampaign);
    document.getElementById('btnSaveEdit').addEventListener('click', handleSaveEdit);

    // 2. Modal Chọn xe
    document.getElementById('btnOpenVehicleModal').addEventListener('click', () => {
        vehicleSelectModal.show();
        document.getElementById('vehicleSearchKeyword').value = '';
        vehicleSearchState.keyword = '';
        vehicleSearchState.page = 0;
        fetchAndRenderVehicleModal();
        updateModalSelectedCount();
    });

    document.getElementById('btnSearchVehicle').addEventListener('click', handleSearchVehicleModal);
    document.getElementById('btnConfirmSelection').addEventListener('click', confirmVehicleSelection);

    // 3. Modal Chọn Phụ tùng (MỚI)
    // SỰ KIỆN PHẦN PHỤ TÙNG (SỬA LẠI ĐOẠN NÀY)
        document.getElementById('btnOpenPartModal').addEventListener('click', () => {
            partSelectModal.show();
            document.getElementById('partSearchKeyword').value = '';

            // Tự động tải danh sách ngay khi mở
            handleSearchPart();
        });

    document.getElementById('btnSearchPart').addEventListener('click', handleSearchPart);

    // 4. Xóa tag VIN (Event Delegation)
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

// ... (Giữ nguyên phần 1: Render Table Campaign) ...
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
        const params = { page: state.page, size: state.size };
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
        else if (camp.status === 'DRAFT') statusBadge = 'bg-warning text-dark';
        else if (camp.status === 'CLOSED') statusBadge = 'bg-danger';

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="ps-4 fw-bold font-monospace text-primary">${camp.code}</td>
            <td>${camp.title}</td>
            <td><span class="badge bg-info text-dark">${camp.type}</span></td>
            <td>${start} - ${end}</td>
            <td><span class="badge ${statusBadge}">${camp.status}</span></td>
            <td class="text-end pe-4">
                <button class="btn btn-sm btn-outline-warning me-1 btn-edit" data-id="${camp.id}"><i class="bi bi-pencil-square"></i></button>
                <button class="btn btn-sm btn-outline-primary me-1 btn-view" data-id="${camp.id}"><i class="bi bi-eye"></i></button>
                <button class="btn btn-sm btn-outline-danger btn-delete" data-id="${camp.id}"><i class="bi bi-trash"></i></button>
            </td>
        `;
        tbody.appendChild(tr);
    });

    document.querySelectorAll('.btn-edit').forEach(btn => btn.addEventListener('click', (e) => openEditModal(e.currentTarget.dataset.id)));
    document.querySelectorAll('.btn-view').forEach(btn => btn.addEventListener('click', (e) => openViewModal(e.currentTarget.dataset.id)));
    document.querySelectorAll('.btn-delete').forEach(btn => btn.addEventListener('click', (e) => handleDeleteCampaign(e.currentTarget.dataset.id)));
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
window.changePageCamp = (page) => { state.page = page; fetchAndRenderCampaigns(); }


// ============================================================
// PHẦN 2: TẠO MỚI (CREATE) - ĐÃ CẬP NHẬT THÊM PARTS
// ============================================================

async function handleCreateCampaign() {
    const btn = document.getElementById('btnSaveCampaign');
    const code = document.getElementById('campCode').value.trim();
    const title = document.getElementById('campTitle').value.trim();
    const type = document.getElementById('campType').value;
    const startAt = document.getElementById('campStart').value;
    const endAt = document.getElementById('campEnd').value;
    const vinText = document.getElementById('campVins').value; // Input ẩn

    if (!code || !title || !startAt || !endAt) {
        alert("Vui lòng nhập đủ thông tin bắt buộc (*)");
        return;
    }

    const vinList = vinText.split('\n').map(v => v.trim()).filter(v => v !== '');

    // --- CHUẨN BỊ DANH SÁCH PHỤ TÙNG ---
    const partsDto = selectedParts.map(p => ({
        partNumber: p.partNumber,
        partName: p.partName,
        quantity: p.quantity
    }));

    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang xử lý...';

    try {
        // Gửi API tạo chiến dịch (kèm Parts)
        const campDto = {
            code, title, type, startAt, endAt,
            parts: partsDto // <--- QUAN TRỌNG
        };

        const newCampaign = await createCampaign(campDto);
        console.log("LOG: Tạo thành công. ID =", newCampaign.id);

        // Import VINs
        if (vinList.length > 0) {
            btn.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Đang import ${vinList.length} xe...`;
            let successCount = 0;
            for (const vin of vinList) {
                try {
                    await addAffectedVehicle(newCampaign.id, { campaignId: newCampaign.id, vehicleVin: vin });
                    successCount++;
                } catch (e) { console.warn(`Lỗi import VIN ${vin}:`, e); }
            }
            alert(`Tạo chiến dịch thành công!\nĐã import: ${successCount}/${vinList.length} xe.`);
        } else {
            alert("Tạo chiến dịch thành công (Chưa có xe nào).");
        }

        createModal.hide();

        // Reset form & state
        document.getElementById('createCampaignForm').reset();
        selectedVins.clear();
        renderSelectedVinTags();

        selectedParts = []; // Reset phụ tùng
        renderSelectedPartsTable(); // Vẽ lại bảng trống

        fetchAndRenderCampaigns();

    } catch (error) {
        alert("Lỗi: " + error.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = 'Lưu & Phát hành';
    }
}

// ... (Giữ nguyên Logic Edit, View, Delete cũ) ...
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
    } catch (error) { alert("Lỗi tải thông tin: " + error.message); }
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
    } catch (error) { alert("Lỗi cập nhật: " + error.message); }
}

async function openViewModal(id) {
    try {
        // 1. Lấy thông tin chi tiết chiến dịch (Kèm danh sách phụ tùng)
        const camp = await getCampaignDetail(id);

        // Fill thông tin cơ bản
        document.getElementById('viewCode').textContent = camp.code || '---';
        document.getElementById('viewTitle').textContent = camp.title || '---';
        document.getElementById('viewType').textContent = camp.type || '---';
        document.getElementById('viewStatus').textContent = camp.status || '---';

        const startStr = camp.startAt ? new Date(camp.startAt).toLocaleString('vi-VN') : '---';
        const endStr = camp.endAt ? new Date(camp.endAt).toLocaleString('vi-VN') : '---';
        document.getElementById('viewStart').textContent = startStr;
        document.getElementById('viewEnd').textContent = endStr;

        // 2. Render Danh sách Phụ tùng (Lấy trực tiếp từ biến camp)
        const partsTbody = document.getElementById('viewPartsTbody');
        partsTbody.innerHTML = '';

        // Kiểm tra kỹ: camp.parts có tồn tại và là mảng không?
        if (camp.parts && Array.isArray(camp.parts) && camp.parts.length > 0) {
            camp.parts.forEach(p => {
                partsTbody.innerHTML += `
                    <tr>
                        <td class="fw-bold">${p.partNumber}</td>
                        <td>${p.partName}</td>
                        <td>${p.quantityLimit}</td>
                    </tr>
                `;
            });
        } else {
            partsTbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted fst-italic">Không có phụ tùng nào được áp dụng.</td></tr>';
        }

        // 3. Gọi API riêng để tải danh sách xe (Vì xe không đi kèm trong object camp)
        loadAffectedVehiclesForView(id);

        // 4. Hiển thị Modal
        viewModal.show();

    } catch (error) {
        console.error(error);
        alert("Lỗi tải chi tiết: " + error.message);
    }
}

async function loadAffectedVehiclesForView(campaignId) {
    const tbody = document.getElementById('viewVehicleTbody');
    tbody.innerHTML = '<tr><td colspan="3" class="text-center"><div class="spinner-border spinner-border-sm text-primary"></div></td></tr>';

    try {
        // Gọi API (size lớn để hiện nhiều)
        const response = await searchAffectedVehicles(campaignId, { size: 100 });

        // ⚠️ SỬA LỖI Ở ĐÂY: Kiểm tra kỹ cấu trúc response
        let vehicles = [];
        if (response && response.content) {
            vehicles = response.content;
        } else if (response && response.data && response.data.content) {
            vehicles = response.data.content;
        }

        tbody.innerHTML = '';
        if (!vehicles || vehicles.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">Chưa có xe nào.</td></tr>';
            return;
        }

        vehicles.forEach((v, index) => {
            let badge = 'bg-secondary';
            if(v.status === 'NOTIFIED') badge = 'bg-info text-dark';
            if(v.status === 'SCHEDULED') badge = 'bg-primary';
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
        console.error("Lỗi tải xe:", error);
        tbody.innerHTML = '<tr><td colspan="3" class="text-center text-danger">Lỗi tải danh sách xe.</td></tr>';
    }
}

async function handleDeleteCampaign(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa chiến dịch này không?')) return;
    try { await deleteCampaign(id); alert('Đã xóa thành công!'); fetchAndRenderCampaigns(); }
    catch (error) { alert('Không thể xóa: ' + error.message); }
}

// ... (Giữ nguyên Logic Chọn Xe cũ) ...
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
    } catch (error) { console.error("Lỗi tìm xe:", error); tbody.innerHTML = `<tr><td colspan="5" class="text-center text-danger">Lỗi: ${error.message}</td></tr>`; }
}
function renderVehicleModalTable(vehicles) {
    const tbody = document.getElementById('vehicleSearchResultBody');
    tbody.innerHTML = '';
    if (vehicles.length === 0) { tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Không tìm thấy xe nào.</td></tr>'; return; }
    vehicles.forEach(v => {
        const isChecked = selectedVins.has(v.vehicleVin) ? 'checked' : '';
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="text-center"><input type="checkbox" class="form-check-input" value="${v.vehicleVin}" ${isChecked} onchange="window.toggleVinSelection('${v.vehicleVin}')"></td>
            <td class="font-monospace fw-bold">${v.vehicleVin}</td><td>${v.model}</td><td>${v.licensePlate || '-'}</td><td>${v.customer ? v.customer.customerName : 'N/A'}</td>`;
        tbody.appendChild(tr);
    });
}
window.toggleVinSelection = (vin) => { if (selectedVins.has(vin)) selectedVins.delete(vin); else selectedVins.add(vin); updateModalSelectedCount(); };
function updateModalSelectedCount() { document.getElementById('modalSelectedCount').textContent = `Đang chọn: ${selectedVins.size} xe`; }
function confirmVehicleSelection() { renderSelectedVinTags(); vehicleSelectModal.hide(); }
function renderSelectedVinTags() {
    const container = document.getElementById('selectedVinContainer');
    const textarea = document.getElementById('campVins');
    const countEl = document.getElementById('vinCount');
    container.innerHTML = '';
    if (selectedVins.size === 0) { container.innerHTML = '<span class="text-muted small fst-italic p-1">Chưa có xe nào được chọn.</span>'; textarea.value = ''; countEl.textContent = 'Đã chọn: 0 xe'; return; }
    selectedVins.forEach(vin => {
        const tag = document.createElement('span');
        tag.className = "badge bg-primary me-1 mb-1 pe-1";
        tag.innerHTML = `${vin} <button type="button" class="btn-close btn-close-white ms-2 btn-remove-vin" style="font-size: 0.5em;" aria-label="Close" data-vin="${vin}"></button>`;
        container.appendChild(tag);
    });
    textarea.value = Array.from(selectedVins).join('\n');
    countEl.textContent = `Đã chọn: ${selectedVins.size} xe`;
}
function renderVehicleModalPagination(pageData) { /* (Giữ nguyên code phân trang xe) */ }
window.changeVehicleModalPage = (page) => { vehicleSearchState.page = page; fetchAndRenderVehicleModal(); }


// ============================================================
// PHẦN 7: LOGIC QUẢN LÝ PHỤ TÙNG (CAMPAIGN PARTS) - MỚI 100%
// ============================================================

// HÀM TÌM KIẾM PHỤ TÙNG (SỬA LẠI LOGIC FETCH)
async function handleSearchPart() {
    const keyword = document.getElementById('partSearchKeyword').value.trim();
    const tbody = document.getElementById('partSearchResultBody');
    tbody.innerHTML = '<tr><td colspan="4" class="text-center"><div class="spinner-border spinner-border-sm"></div></td></tr>';

    try {
        // Gọi API
        console.log("LOG: Calling searchParts with keyword:", keyword);
        const response = await searchParts({ name: keyword, page: 0, size: 10 });
        console.log("LOG: API Response:", response); // <-- Quan trọng để debug

        // Xử lý dữ liệu trả về (Linh hoạt với nhiều kiểu response)
        let parts = [];
        if (response && response.data && Array.isArray(response.data.content)) {
            // Trường hợp 1: API trả về Page chuẩn { data: { content: [...] } }
            parts = response.data.content;
        } else if (response && Array.isArray(response.content)) {
            // Trường hợp 2: API trả về Page trực tiếp { content: [...] }
            parts = response.content;
        } else if (Array.isArray(response)) {
            // Trường hợp 3: API trả về List [...]
            parts = response;
        } else {
            // Trường hợp 4: Data nằm ở lớp ngoài cùng (response.data)
             parts = response.data || [];
        }

        tbody.innerHTML = '';
        if (!parts || parts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">Không tìm thấy phụ tùng nào.</td></tr>';
            return;
        }

    parts.forEach(p => {
                    // --- LOGIC TÍNH TỒN KHO (ĐÃ SỬA THEO API THỰC TẾ) ---
                    let totalStock = 0;

                                // ƯU TIÊN SỐ 1: Trường 'inventoryQuantity' (Như trong hình Console bạn gửi)
                    if (p.inventoryQuantity !== undefined && p.inventoryQuantity !== null) {
                        totalStock = p.inventoryQuantity;
                         }
                    // Các trường hợp dự phòng (giữ lại để an toàn nếu API thay đổi)
                    else if (p.quantityInStock !== undefined) {
                        totalStock = p.quantityInStock;
                        }
                    else if (p.inventories && Array.isArray(p.inventories)) {
                         totalStock = p.inventories.reduce((sum, inv) => sum + (inv.quantity || 0), 0);
                        }

                    const code = p.serialNumber || p.partType; // Lấy mã chuẩn
                    const isSelected = selectedParts.some(sp => sp.partNumber === code);

                    // Truyền thêm totalStock vào hàm selectPart để lát còn validate
                    const btnState = isSelected ? 'disabled class="btn btn-sm btn-secondary"' : `class="btn btn-sm btn-success" onclick="window.selectPart('${code}', '${p.name}', ${totalStock})"`;
                    const btnText = isSelected ? 'Đã thêm' : '<i class="bi bi-plus"></i> Thêm';

                    // Nếu hết hàng -> Disable nút thêm luôn
                    const stockDisplay = totalStock > 0 ? `<span class="text-success fw-bold">${totalStock}</span>` : `<span class="text-danger fw-bold">Hết hàng</span>`;
                    const finalBtn = (totalStock <= 0) ? `<button class="btn btn-sm btn-secondary" disabled>Hết hàng</button>` : `<button ${btnState}>${btnText}</button>`;

                    tbody.innerHTML += `
                        <tr>
                            <td class="fw-bold">${code}</td>
                            <td>${p.name}</td>
                            <td>${stockDisplay}</td>
                            <td class="text-end">${finalBtn}</td>
                        </tr>
                    `;
                });

            } catch (error) {
                console.error(error);
                tbody.innerHTML = '<tr><td colspan="4" class="text-center text-danger">Lỗi tìm kiếm.</td></tr>';
            }
        }

// Hàm chọn phụ tùng
// Sửa hàm này để nhận thêm tham số maxStock
window.selectPart = (partNumber, partName, maxStock) => {
    selectedParts.push({
        partNumber,
        partName,
        quantity: 1,
        maxStock: maxStock // Lưu lại maxStock để check
    });
    renderSelectedPartsTable();
    partSelectModal.hide();
};

// Hàm xóa phụ tùng
window.removePart = (partNumber) => {
    selectedParts = selectedParts.filter(p => p.partNumber !== partNumber);
    renderSelectedPartsTable();
};

// Hàm cập nhật số lượng
// Hàm update số lượng (Có validate)
window.updatePartQuantity = (partNumber, inputEl) => {
    let newQty = parseInt(inputEl.value);
    const part = selectedParts.find(p => p.partNumber === partNumber);

    if (part) {
        // Validate số lượng
        if (newQty > part.maxStock) {
            alert(`Số lượng không được vượt quá tồn kho (${part.maxStock})!`);
            newQty = part.maxStock; // Reset về max
            inputEl.value = newQty;
        }
        if (newQty < 1) {
            newQty = 1;
            inputEl.value = 1;
        }
        part.quantity = newQty;
    }
};


// Sửa hàm render để thêm thuộc tính max vào input
function renderSelectedPartsTable() {
    const tbody = document.getElementById('selectedPartsTbody');
    tbody.innerHTML = '';

    if (selectedParts.length === 0) {
        tbody.innerHTML = '<tr id="noPartsRow"><td colspan="4" class="text-center text-muted fst-italic">Chưa có phụ tùng nào.</td></tr>';
        return;
    }

    selectedParts.forEach(p => {
        tbody.innerHTML += `
            <tr>
                <td class="fw-bold">${p.partNumber}</td>
                <td>${p.partName}</td>
                <td>
                    <div class="input-group input-group-sm" style="width: 100px">
                        <input type="number" class="form-control text-center"
                               value="${p.quantity}" min="1" max="${p.maxStock}"
                               onchange="window.updatePartQuantity('${p.partNumber}', this)">
                    </div>
                    <div class="form-text text-muted" style="font-size: 0.7em">Tối đa: ${p.maxStock}</div>
                </td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-danger" onclick="window.removePart('${p.partNumber}')">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>
        `;
    });
}