// src/JS/evm-campaigns.js
import {
    searchCampaigns,
    createCampaign,
    addAffectedVehicle // Import hàm này từ service
} from '../services/campaignService.js';

let state = {
    page: 0,
    size: 10,
    code: '',
    status: ''
};

let createModal = null;

/**
 * Hàm khởi tạo (Gọi từ evmStaff.js)
 */
export function setupCampaignManagement() {
    console.log("LOG: Init Campaign Management JS...");

    const modalEl = document.getElementById('createCampaignModal');
    if (modalEl) createModal = new bootstrap.Modal(modalEl);

    // Gán sự kiện
    document.getElementById('btnFilter').addEventListener('click', handleFilter);
    document.getElementById('btnSaveCampaign').addEventListener('click', handleCreateCampaign);

    // Đếm số dòng VIN khi nhập
    document.getElementById('campVins').addEventListener('input', (e) => {
        const lines = e.target.value.split('\n').filter(line => line.trim() !== '');
        document.getElementById('vinCount').textContent = `Đã nhập: ${lines.length} VIN`;
    });

    // Tải dữ liệu lần đầu
    fetchAndRenderCampaigns();
}

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

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="ps-4 fw-bold font-monospace text-primary">${camp.code}</td>
            <td>${camp.title}</td>
            <td><span class="badge bg-info text-dark">${camp.type}</span></td>
            <td>${start} - ${end}</td>
            <td><span class="badge ${statusBadge}">${camp.status}</span></td>
            <td class="text-end pe-4">
                <button class="btn btn-sm btn-outline-primary" title="Xem chi tiết">
                    <i class="bi bi-eye"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger ms-1" title="Xóa/Đóng">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function renderPagination(pageData) {
    // (Logic phân trang giống hệt các trang trước, bạn có thể copy lại)
    const paginationUl = document.getElementById('pagination');
    paginationUl.innerHTML = '';
    const { totalPages, number: currentPage } = pageData;
    if (totalPages <= 1) return;

    // Vẽ đơn giản Pre/Next
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
// LOGIC: TẠO CHIẾN DỊCH & IMPORT VIN
// ============================================================

async function handleCreateCampaign() {
    const btn = document.getElementById('btnSaveCampaign');

    // 1. Lấy dữ liệu Form
    const code = document.getElementById('campCode').value.trim();
    const title = document.getElementById('campTitle').value.trim();
    const type = document.getElementById('campType').value;
    const startAt = document.getElementById('campStart').value;
    const endAt = document.getElementById('campEnd').value;
    const vinText = document.getElementById('campVins').value;

    if (!code || !title || !startAt || !endAt) {
        alert("Vui lòng nhập đủ thông tin bắt buộc (*)");
        return;
    }

    // Lọc danh sách VIN (bỏ dòng trống)
    const vinList = vinText.split('\n').map(v => v.trim()).filter(v => v !== '');

    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang xử lý...';

    try {
        // BƯỚC 1: Tạo Chiến dịch
        console.log("LOG: Đang tạo chiến dịch...");
        const campDto = { code, title, type, startAt, endAt };

        // Gọi API tạo (Server trả về object Campaign đã tạo, có ID)
        const newCampaign = await createCampaign(campDto);
        console.log("LOG: Tạo thành công. ID =", newCampaign.id);

        // BƯỚC 2: Import VINs (Nếu có)
        if (vinList.length > 0) {
            btn.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Đang import ${vinList.length} xe...`;

            // Gọi vòng lặp hoặc API bulk (tạm thời dùng loop cho chắc ăn nếu chưa có API bulk)
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
        document.getElementById('createCampaignForm').reset();
        document.getElementById('vinCount').textContent = 'Đã nhập: 0 VIN';
        fetchAndRenderCampaigns(); // Reload bảng

    } catch (error) {
        alert("Lỗi: " + error.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = 'Lưu & Phát hành';
    }
}