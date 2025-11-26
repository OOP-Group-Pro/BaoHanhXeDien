import { api } from '../services/apiClient.js';
import {
    getVehicleByVin,
    getHistoryByVehicleId,
    getPartsByVehicleId,
    getServiceHistoryDetail
} from '../services/vehicleService.js';
import { checkCampaignEligibility } from '../services/campaignService.js';

// --- CẤU HÌNH CHÍNH SÁCH BẢO HÀNH ---
// Bạn nên lấy cái này từ API Policy nếu có thể, nhưng tạm thời hardcode cho MVP
const WARRANTY_POLICY = {
    maxYears: 10, // VF bảo hành 10 năm
    maxKm: 200000
};

/**
 * Hàm khởi tạo module Tra cứu xe
 */
export function setupVehicleLookup() {
    console.log("🚗 [Vehicle Lookup] Module initialized.");

    const searchForm = document.getElementById('searchForm');
    if (!searchForm) return;

    searchForm.addEventListener('submit', handleSearch);
}

// --- LOGIC TÌM KIẾM ---
async function handleSearch(e) {
    e.preventDefault();
    const vinInput = document.getElementById('vinInput').value.trim().toUpperCase();
    const searchBtn = document.getElementById('searchBtn');
    const resultSection = document.getElementById('resultSection');
    const errorDiv = document.getElementById('searchError');

    // Validate
    if (!vinInput || vinInput.length !== 17) {
        if (errorDiv) errorDiv.textContent = 'Vui lòng nhập đúng 17 ký tự VIN.';
        else alert('Vui lòng nhập đúng 17 ký tự VIN.');
        return;
    }

    // Reset UI
    if (errorDiv) errorDiv.textContent = "";
    searchBtn.disabled = true;
    searchBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang tìm...';
    resultSection.classList.add('d-none');

    try {
        // BƯỚC 1: Lấy thông tin xe
        console.log(`🔍 Searching vehicle: ${vinInput}`);
        const vehicleResponse = await getVehicleByVin(vinInput);

        // Handle wrapper data (nếu có)
        const vehicleData = vehicleResponse.data || vehicleResponse;

        if (!vehicleData || !vehicleData.vehicleId) {
            throw new Error("Không tìm thấy thông tin xe hoặc dữ liệu trả về lỗi.");
        }

        console.log("✅ Vehicle Found:", vehicleData);

        // BƯỚC 2: Gọi API phụ thuộc song song
        const [historyData, partsData, campaignData] = await Promise.all([
            getHistoryByVehicleId(vehicleData.vehicleId),
            getPartsByVehicleId(vehicleData.vehicleId),
            // Campaign có thể lỗi 404 nếu không có chiến dịch, nên catch riêng
            checkCampaignEligibility(vinInput).catch(err => {
                console.warn("⚠️ Lỗi check campaign (có thể bỏ qua):", err);
                return [];
            })
        ]);

        // BƯỚC 3: Render
        renderVehicleInfo(vehicleData);
        renderWarrantyStatus(vehicleData);

        // Xử lý data wrapper cho các list (nếu backend trả về Page hoặc List)
        const histories = Array.isArray(historyData) ? historyData : (historyData.data || []);
        const parts = Array.isArray(partsData) ? partsData : (partsData.data || []);

        // Xử lý campaign data (hơi lằng nhằng do API có thể trả về nhiều kiểu)
        let campaigns = [];
        if (Array.isArray(campaignData)) campaigns = campaignData;
        else if (campaignData && Array.isArray(campaignData.data)) campaigns = campaignData.data;

        renderHistoryTable(histories);
        renderPartsTable(parts);
        renderCampaignStatus(campaigns);

        // Show kết quả
        resultSection.classList.remove('d-none');

    } catch (error) {
        console.error("❌ Search Error:", error);
        if (errorDiv) errorDiv.textContent = error.message || "Lỗi kết nối server.";
        else alert(error.message);
    } finally {
        searchBtn.disabled = false;
        searchBtn.innerHTML = '<i class="bi bi-search me-2"></i> Tìm kiếm';
    }
}

// --- RENDER FUNCTIONS ---

function renderVehicleInfo(vehicle) {
    document.getElementById('displayVin').textContent = vehicle.vehicleVin;
    document.getElementById('displayModel').textContent = vehicle.model || 'Unknown Model';
    document.getElementById('displayPlate').textContent = vehicle.licensePlate || 'Chưa ĐK';
    document.getElementById('displayCustomer').textContent = vehicle.customer ? vehicle.customer.customerName : 'N/A';
}

function renderWarrantyStatus(vehicle) {
    const startDate = vehicle.warrantyStartDate ? new Date(vehicle.warrantyStartDate) : null;
    const currentOdo = vehicle.currentOdometer || 0;

    const elStartDate = document.getElementById('warrantyStartDate');
    const elCurrentOdo = document.getElementById('currentOdometer');
    const elBadge = document.getElementById('warrantyStatusBadge');
    const elTimeText = document.getElementById('warrantyTimeText');
    const elOdoText = document.getElementById('warrantyOdoText');

    elCurrentOdo.textContent = currentOdo.toLocaleString('vi-VN');

    if (!startDate) {
        elStartDate.textContent = "Chưa kích hoạt";
        elBadge.className = "badge bg-secondary";
        elBadge.textContent = "CHƯA KÍCH HOẠT";
        updateProgressBar('warrantyTimeProgress', 0);
        updateProgressBar('warrantyOdoProgress', 0);
        elTimeText.textContent = "-- / --";
        elOdoText.textContent = "-- / --";
        return;
    }

    elStartDate.textContent = startDate.toLocaleDateString('vi-VN');

    // Tính toán thời gian
    const now = new Date();
    const endDate = new Date(startDate);
    endDate.setFullYear(endDate.getFullYear() + WARRANTY_POLICY.maxYears);

    const totalTime = endDate.getTime() - startDate.getTime();
    const usedTime = now.getTime() - startDate.getTime();

    // Tính % (giới hạn 0-100)
    let timePercent = (usedTime / totalTime) * 100;
    let odoPercent = (currentOdo / WARRANTY_POLICY.maxKm) * 100;

    // Hiển thị text
    const yearsUsed = (usedTime / (1000 * 60 * 60 * 24 * 365)).toFixed(1);
    elTimeText.textContent = `${yearsUsed} / ${WARRANTY_POLICY.maxYears} Năm`;
    updateProgressBar('warrantyTimeProgress', timePercent);

    elOdoText.textContent = `${currentOdo.toLocaleString()} / ${WARRANTY_POLICY.maxKm.toLocaleString()} Km`;
    updateProgressBar('warrantyOdoProgress', odoPercent);

    // Quyết định trạng thái
    if (timePercent >= 100 || odoPercent >= 100) {
        elBadge.className = "badge bg-danger";
        elBadge.textContent = "HẾT HẠN";
    } else if (timePercent > 80 || odoPercent > 80) {
        elBadge.className = "badge bg-warning text-dark";
        elBadge.textContent = "SẮP HẾT HẠN";
    } else {
        elBadge.className = "badge bg-success";
        elBadge.textContent = "ĐANG BẢO HÀNH";
    }
}

function updateProgressBar(elementId, percent) {
    const el = document.getElementById(elementId);
    if(!el) return;

    const finalPercent = Math.min(Math.max(percent, 0), 100);
    el.style.width = `${finalPercent}%`;

    // Đổi màu thanh progress
    el.className = 'progress-bar'; // Reset class
    if (finalPercent >= 100) el.classList.add('bg-danger');
    else if (finalPercent > 80) el.classList.add('bg-warning');
    else el.classList.add('bg-success');
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
        row.style.cursor = 'pointer'; // Thêm con trỏ tay

        const dateStr = new Date(item.performedDate).toLocaleDateString('vi-VN');

        row.innerHTML = `
            <td class="ps-4 fw-bold">${dateStr}</td>
            <td>${item.description}</td>
            <td class="fw-bold text-primary">${item.odometerReading?.toLocaleString() || 0} Km</td>
            <td>
                <div class="small fw-bold">${item.centerName || 'Trạm dịch vụ'}</div>
                <div class="small text-muted" style="font-size: 11px;">KTV: ${item.technicianName || 'Unknown'}</div>
            </td>
            <td class="text-end pe-4">
                <button class="btn btn-sm btn-outline-primary btn-view-detail" data-id="${item.serviceHistoryId}">
                    <i class="bi bi-eye"></i>
                </button>
            </td>
        `;

        // Click vào cả hàng cũng mở modal
        row.addEventListener('click', (e) => {
            // Tránh sự kiện click đúp nếu ấn vào nút button
            if(!e.target.closest('button')) {
                handleViewHistoryDetail(item.serviceHistoryId);
            }
        });

        tbody.appendChild(row);
    });

    // Gắn sự kiện cho nút button riêng lẻ (dự phòng)
    document.querySelectorAll('.btn-view-detail').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation(); // Ngăn sự kiện click row
            const historyId = e.currentTarget.dataset.id;
            handleViewHistoryDetail(historyId);
        });
    });
}

async function handleViewHistoryDetail(historyId) {
    const modalEl = document.getElementById('historyDetailModal');
    const partsBody = document.getElementById('modalPartsTable');

    // Reset modal content
    if(partsBody) partsBody.innerHTML = '<tr><td colspan="3" class="text-center"><div class="spinner-border spinner-border-sm"></div></td></tr>';

    // Open modal
    let modalInstance;
    if (typeof bootstrap !== 'undefined' && modalEl) {
        modalInstance = bootstrap.Modal.getOrCreateInstance(modalEl);
        modalInstance.show();
    }

    try {
        const response = await getServiceHistoryDetail(historyId);
        console.log("📦 History Detail:", response);

        // Logic lấy data thông minh (như đã bàn)
        let data = response.data || response.result || response;
        if (!data || !data.performedDate) throw new Error("Dữ liệu chi tiết trống.");

        // Fill data
        document.getElementById('modalDate').textContent = new Date(data.performedDate).toLocaleString('vi-VN');
        document.getElementById('modalOdo').textContent = (data.odometerReading?.toLocaleString() || 0) + ' Km';
        document.getElementById('modalDesc').textContent = data.description || 'Không có mô tả';

        // Fill parts table
        if (partsBody) {
            partsBody.innerHTML = '';
            if (data.parts && data.parts.length > 0) {
                data.parts.forEach(p => {
                    partsBody.innerHTML += `
                        <tr>
                            <td>${p.partName || 'N/A'}</td>
                            <td>${p.partNumber || 'N/A'}</td>
                            <td class="font-monospace fw-bold text-primary">${p.serialNumber || 'N/A'}</td>
                        </tr>
                    `;
                });
            } else {
                partsBody.innerHTML = '<tr><td colspan="3" class="text-center text-muted py-3">Không có phụ tùng thay thế</td></tr>';
            }
        }

    } catch (error) {
        console.error(error);
        alert('Lỗi tải chi tiết: ' + error.message);
        if(modalInstance) modalInstance.hide();
    }
}

function renderPartsTable(parts) {
    const tbody = document.getElementById('partsTableBody');
    tbody.innerHTML = '';

    if (!parts || parts.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-3">Chưa có linh kiện lắp đặt.</td></tr>';
        return;
    }

    parts.forEach(p => {
        const dateStr = p.installDate ? new Date(p.installDate).toLocaleDateString('vi-VN') : '-';
        const statusClass = p.status === 'INSTALLED' ? 'bg-success' : 'bg-secondary';

        tbody.innerHTML += `
            <tr>
                <td class="ps-4">
                    <div class="fw-bold">${p.partName || 'Part #' + p.partId}</div>
                    <small class="text-muted">${p.partNumber || ''}</small>
                </td>
                <td class="font-monospace">${p.serialNumber}</td>
                <td>${dateStr}</td>
                <td><span class="badge ${statusClass}">${p.status}</span></td>
            </tr>
        `;
    });
}

function renderCampaignStatus(campaigns) {
    const campaignCard = document.getElementById('campaignCard');
    const noCampaignCard = document.getElementById('noCampaignCard');
    const container = document.getElementById('campaignListContainer');

    if (!campaigns || campaigns.length === 0) {
        if(campaignCard) campaignCard.style.display = 'none';
        if(noCampaignCard) noCampaignCard.style.display = 'block';
        return;
    }

    if(noCampaignCard) noCampaignCard.style.display = 'none';
    if(campaignCard) campaignCard.style.display = 'block';
    if(container) {
        container.innerHTML = '';
        campaigns.forEach(camp => {
            let partsHtml = '';
            if (camp.parts && camp.parts.length > 0) {
                partsHtml = '<ul class="mb-0 small mt-1 text-muted ps-3">';
                camp.parts.forEach(p => {
                    partsHtml += `<li>${p.partName} (x${p.quantityLimit})</li>`;
                });
                partsHtml += '</ul>';
            }

            const div = document.createElement('div');
            div.className = 'alert alert-warning mb-2 p-2 border-warning';
            div.innerHTML = `
                <div class="d-flex justify-content-between align-items-center">
                    <strong style="font-size: 0.9rem;">${camp.code}: ${camp.title}</strong>
                    <span class="badge bg-warning text-dark" style="font-size: 0.7rem;">${camp.type}</span>
                </div>
                ${partsHtml}
            `;
            container.appendChild(div);
        });
    }
}

// Gán hàm viewHistoryDetail vào window để HTML gọi được (nếu cần)
window.viewHistoryDetail = handleViewHistoryDetail;