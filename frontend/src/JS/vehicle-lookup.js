// src/vehicleLookup.js

// 1. Import các service (như cũ)
import {
    getServiceHistoryDetail,
    getVehicleByVin, getHistoryByVehicleId, getPartsByVehicleId
} from '../services/vehicleService.js';
// (Xóa import renderStaffSidebar vì scStaff.js sẽ lo việc render layout)

// --- CẤU HÌNH CHÍNH SÁCH BẢO HÀNH (GIỮ NGUYÊN) ---
const WARRANTY_POLICY = {
    maxYears: 3,
    maxKm: 100000
};

// ❌ XÓA ĐOẠN TỰ CHẠY NÀY
/*
document.addEventListener('DOMContentLoaded', () => {
    renderStaffSidebar();
    setupEventListeners();
});
*/

// ✅ THAY BẰNG HÀM EXPORT NÀY
/**
 * Hàm khởi tạo logic cho trang Tra cứu xe
 * (Được gọi bởi scStaff.js khi vào đúng trang)
 */
export function setupVehicleLookup() {
    console.log("LOG: Đang khởi tạo module Tra cứu xe...");

    const searchForm = document.getElementById('searchForm');

    // Nếu không tìm thấy form (nghĩa là đang ở trang khác), thoát ngay
    if (!searchForm) return;

    // Gắn sự kiện (Logic giữ nguyên từ hàm setupEventListeners và handleSearch cũ)
    searchForm.addEventListener('submit', handleSearch);
}

// --- LOGIC XỬ LÝ TÌM KIẾM (GIỮ NGUYÊN 100%) ---
async function handleSearch(e) {
    e.preventDefault();
    const vinInput = document.getElementById('vinInput').value.trim();
    // ... (Code lấy button, resultSection, errorEl để update UI)
    const searchBtn = document.getElementById('searchBtn');
    const resultSection = document.getElementById('resultSection');
    const errorEl = document.getElementById('searchError');

    if (!vinInput || vinInput.length !== 17) {
        // (Sửa nhẹ: hiển thị lỗi vào div thay vì alert để UX tốt hơn, hoặc giữ alert tùy bạn)
        if(errorEl) errorEl.textContent = 'Vui lòng nhập đúng 17 ký tự VIN.';
        else alert('Vui lòng nhập đúng 17 ký tự VIN.');
        return;
    }

    // Reset UI
    if(errorEl) errorEl.textContent = "";
    searchBtn.disabled = true;
    searchBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang tìm...';
    resultSection.classList.add('d-none');

    try {
        // 1. Gọi API lấy thông tin xe
        const vehicleData = await getVehicleByVin(vinInput);

        // (Kiểm tra xem vehicleData có hợp lệ không)
        if (!vehicleData || !vehicleData.data) {
            throw new Error("Không tìm thấy xe.");
        }

        // 2. Gọi API lấy các dữ liệu phụ
        const [historyData, partsData] = await Promise.all([
            getHistoryByVehicleId(vehicleData.data.vehicleId),
            getPartsByVehicleId(vehicleData.data.vehicleId)
        ]);

        // 3. Render giao diện
        renderVehicleInfo(vehicleData.data);
        renderWarrantyStatus(vehicleData.data);
        renderHistoryTable(historyData.data);
        renderPartsTable(partsData.data);

        // 4. Hiển thị vùng kết quả
        resultSection.classList.remove('d-none');

    } catch (error) {
        console.error(error);
        if(errorEl) errorEl.textContent = 'Lỗi: ' + error.message;
        else alert('Không tìm thấy xe hoặc có lỗi xảy ra: ' + error.message);
        resultSection.classList.add('d-none');
    } finally {
        searchBtn.disabled = false;
        searchBtn.innerHTML = '<i class="bi bi-search me-2"></i> Tìm kiếm';
    }
}

// --- CÁC HÀM RENDER (GIỮ NGUYÊN 100%) ---

function renderVehicleInfo(vehicle) {
    document.getElementById('displayVin').textContent = vehicle.vehicleVin;
    document.getElementById('displayModel').textContent = vehicle.model;
    document.getElementById('displayPlate').textContent = vehicle.licensePlate || 'Chưa ĐK';
    document.getElementById('displayCustomer').textContent = vehicle.customer ? vehicle.customer.customerName : 'N/A';
}

function renderWarrantyStatus(vehicle) {
    const startDate = vehicle.warrantyStartDate ? new Date(vehicle.warrantyStartDate) : null;
    const currentOdo = vehicle.currentOdometer || 0;

    const elStartDate = document.getElementById('warrantyStartDate');
    const elCurrentOdo = document.getElementById('currentOdometer');
    const elBadge = document.getElementById('warrantyStatusBadge');

    elCurrentOdo.textContent = currentOdo.toLocaleString('vi-VN');

    if (!startDate) {
        elStartDate.textContent = "Chưa kích hoạt";
        elBadge.className = "badge bg-secondary";
        elBadge.textContent = "CHƯA KÍCH HOẠT";
        updateProgressBar('warrantyTimeProgress', 0);
        updateProgressBar('warrantyOdoProgress', 0);
        return;
    }

    elStartDate.textContent = startDate.toLocaleDateString('vi-VN');

    // Tính toán
    const now = new Date();
    const endDate = new Date(startDate);
    endDate.setFullYear(endDate.getFullYear() + WARRANTY_POLICY.maxYears);

    const totalTime = endDate.getTime() - startDate.getTime();
    const usedTime = now.getTime() - startDate.getTime();
    let timePercent = (usedTime / totalTime) * 100;

    let odoPercent = (currentOdo / WARRANTY_POLICY.maxKm) * 100;

    document.getElementById('warrantyTimeText').textContent =
        `${usedTime > 0 ? Math.floor(usedTime / (1000 * 60 * 60 * 24)) : 0} / ${365 * WARRANTY_POLICY.maxYears} ngày`;
    updateProgressBar('warrantyTimeProgress', timePercent);

    document.getElementById('warrantyOdoText').textContent =
        `${currentOdo.toLocaleString()} / ${WARRANTY_POLICY.maxKm.toLocaleString()} Km`;
    updateProgressBar('warrantyOdoProgress', odoPercent);

    if (timePercent >= 100 || odoPercent >= 100) { // (Sửa nhẹ logic OR cho đúng)
        elBadge.className = "badge bg-danger";
        elBadge.textContent = "HẾT HẠN";
    } else {
        elBadge.className = "badge bg-success";
        elBadge.textContent = "ĐANG BẢO HÀNH";
    }
}

function updateProgressBar(elementId, percent) {
    const el = document.getElementById(elementId);
    const finalPercent = Math.min(Math.max(percent, 0), 100);
    el.style.width = `${finalPercent}%`;

    if (finalPercent >= 100) el.classList.replace('bg-success', 'bg-danger');
    else if (finalPercent > 80) el.classList.replace('bg-success', 'bg-warning');
}

function renderHistoryTable(histories) {
    const tbody = document.getElementById('historyTableBody');
    tbody.innerHTML = '';

    if (!histories || histories.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-3">Chưa có lịch sử dịch vụ</td></tr>';
        return;
    }

    histories.forEach(item => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="ps-4">${new Date(item.performedDate).toLocaleDateString('vi-VN')}</td>
            <td>${item.description}</td>
            <td class="fw-bold text-primary">${item.odometerReading?.toLocaleString() || 0} Km</td>
            <td>
                <div class="small fw-bold">${item.centerName || 'N/A'}</div>
                <div class="small text-muted">${item.technicianName || 'N/A'}</div>
            </td>
            <td class="text-end pe-4">
                <button class="btn btn-sm btn-outline-primary btn-view-detail" data-id="${item.serviceHistoryId}">
                    <i class="bi bi-eye"></i> Xem
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });

    document.querySelectorAll('.btn-view-detail').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const historyId = e.currentTarget.getAttribute('data-id');
            handleViewHistoryDetail(historyId);
        });
    });
}

// (Hàm handleViewHistoryDetail giữ nguyên)
async function handleViewHistoryDetail(historyId) {
    try {
        const response = await getServiceHistoryDetail(historyId);
        const data = response.data;

        document.getElementById('modalDate').textContent = new Date(data.performedDate).toLocaleString('vi-VN');
        document.getElementById('modalOdo').textContent = (data.odometerReading?.toLocaleString() || 0) + ' Km';
        document.getElementById('modalDesc').textContent = data.description;

        const partsBody = document.getElementById('modalPartsTable');
        partsBody.innerHTML = '';

        if (data.parts && data.parts.length > 0) {
            data.parts.forEach(p => {
                partsBody.innerHTML += `
                    <tr>
                        <td>${p.partName || 'N/A'}</td>
                        <td>${p.partId}</td>
                        <td class="font-monospace fw-bold">${p.serialNumber || 'N/A'}</td>
                    </tr>
                `;
            });
        } else {
            partsBody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">Không có phụ tùng thay thế trong lần này</td></tr>';
        }

        const modalEl = document.getElementById('historyDetailModal');
        // (Kiểm tra bootstrap có tồn tại không)
        if (typeof bootstrap !== 'undefined') {
            const modalInstance = new bootstrap.Modal(modalEl);
            modalInstance.show();
        } else {
            console.error("Bootstrap JS chưa load!");
        }

    } catch (error) {
        console.error(error);
        alert('Không thể tải chi tiết: ' + error.message);
    }
}

function renderPartsTable(parts) {
    const tbody = document.getElementById('partsTableBody');
    tbody.innerHTML = '';

    if (!parts || parts.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-3">Chưa có thông tin linh kiện</td></tr>';
        return;
    }

    parts.forEach(part => {
        const row = `
            <tr>
                <td class="ps-4">
                    <div class="fw-bold">${part.partName || 'Part #' + part.partId}</div>
                    <small class="text-muted">${part.partNumber || ''}</small>
                </td>
                <td class="font-monospace">${part.serialNumber}</td>
                <td>${part.installDate ? new Date(part.installDate).toLocaleDateString('vi-VN') : '--'}</td>
                <td><span class="badge bg-info text-dark">${part.status}</span></td>
            </tr>
        `;
        tbody.insertAdjacentHTML('beforeend', row);
    });
}