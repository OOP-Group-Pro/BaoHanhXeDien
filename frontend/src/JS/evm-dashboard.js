import { getClaims, getClaimDetails, approveClaim, rejectClaim } from '../services/warrantyService.js';
import { getUsersByRole } from '../services/userService.js';
import { getVehicleByVin } from '../services/vehicleService.js';
import { getPartsDetails } from '../services/partService.js'; // ⬅️ Đã có hàm này từ bước 1

let reviewModalInstance = null;
let currentClaimId = null;

export function setupEvmDashboard() {
    console.log("LOG: Init Dashboard EVM Logic...");
    loadEvmStats();
    loadApprovalQueue();
    initReviewModal();
}

// --- 1. KPI & LIST ---
async function loadEvmStats() {
    try {
        const [pending, approved] = await Promise.all([
            getClaims({ status: 'WAITING_APPROVAL', size: 1 }),
            getClaims({ status: 'APPROVED', size: 1 })
        ]);
        document.getElementById('kpi-pending').textContent = pending.totalElements;
        document.getElementById('kpi-approved').textContent = approved.totalElements;
    } catch(e) { console.error(e); }
}

async function loadApprovalQueue() {
    const tbody = document.getElementById('approval-table-body');
    tbody.innerHTML = '<tr><td colspan="5" class="text-center">Đang tải...</td></tr>';

    try {
        const data = await getClaims({ status: 'WAITING_APPROVAL', size: 20 });
        tbody.innerHTML = '';

        if (data.empty) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">Không có yêu cầu nào cần duyệt.</td></tr>';
            return;
        }

        data.content.forEach(claim => {
            const desc = claim.description && claim.description.length > 40
                ? claim.description.substring(0, 40) + '...'
                : (claim.description || '');

            tbody.innerHTML += `
                <tr>
                    <td><span class="fw-bold text-primary">${claim.claimCode}</span></td>
                    <td>${claim.vin}</td>
                    <td>${desc}</td>
                    <td>${new Date(claim.dateCreated).toLocaleDateString('vi-VN')}</td>
                    <td>
                        <button class="btn btn-sm btn-primary btn-review" 
                                data-id="${claim.claimCode}" 
                                data-code="${claim.claimCode}">
                            <i class="fa-solid fa-magnifying-glass"></i> Thẩm định
                        </button>
                    </td>
                </tr>
            `;
        });

        document.querySelectorAll('.btn-review').forEach(btn => {
            btn.addEventListener('click', (e) => {
                openReviewModal(e.currentTarget.dataset.id, e.currentTarget.dataset.code);
            });
        });

    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-danger text-center">Lỗi: ${e.message}</td></tr>`;
    }
}

// --- 2. MODAL THẨM ĐỊNH ---

function initReviewModal() {
    const modalEl = document.getElementById('reviewModal');
    if (modalEl) reviewModalInstance = new bootstrap.Modal(modalEl);

    document.getElementById('btn-approve-action')?.addEventListener('click', handleApproveClick);
    document.getElementById('btn-reject-action')?.addEventListener('click', handleRejectClick);
}

async function openReviewModal(id, code) {
    document.getElementById('modal-claim-code').textContent = code;
    reviewModalInstance.show();

    const body = document.getElementById('modal-body-content');
    body.innerHTML = '<div class="text-center p-5"><div class="spinner-border text-primary"></div><p class="mt-2">Đang phân tích dữ liệu...</p></div>';

    try {
        // 1. Lấy chi tiết Claim
        const claim = await getClaimDetails(code);
        currentClaimId = claim.claimCode;
        const partNumbers = claim.partList.map(p => p.partNumber); // List các partType

        // 2. GỌI 3 API SONG SONG
        const [vehicleRes, partsMap, technicians] = await Promise.all([
            getVehicleByVin(claim.vin),           // Lấy ODO, Ngày mua
            getPartsDetails(partNumbers),         // Lấy Tồn kho, Policy
            getUsersByRole('SC_TECHNICIAN')       // Lấy DS KTV
        ]);

        // 3. Render giao diện thông minh
        renderSmartReview(claim, vehicleRes.data, partsMap, technicians);

    } catch (e) {
        console.error(e);
        body.innerHTML = `<div class="alert alert-danger">Lỗi tải dữ liệu: ${e.message}</div>`;
    }
}

function renderSmartReview(claim, vehicle, partsMap, technicians) {
    const body = document.getElementById('modal-body-content');

    // A. Render Dropdown KTV
    let techOptions = '<option value="">-- Chọn Kỹ thuật viên --</option>';
    if (technicians && technicians.length > 0) {
        techOptions += technicians.map(t => `<option value="${t.userId}">${t.fullName}</option>`).join('');
    }

    // B. Render Danh sách Phụ tùng & Logic kiểm tra (Giữ nguyên)
    let totalCost = 0;
    let canApprove = true;
    let warningMessages = [];

    let partsHtml = '';
    if (!claim.partList || claim.partList.length === 0) {
        partsHtml = '<li class="list-group-item text-muted">Không có phụ tùng yêu cầu</li>';
    } else {
        partsHtml = claim.partList.map(p => {
            const info = partsMap[p.partNumber] || {};
            const stock = info.inventoryQuantity || 0;
            const price = info.price || 0;
            const lineTotal = price * p.quantityRequired;
            totalCost += lineTotal;

            // 1. Check Tồn kho
            const isStockOk = stock >= p.quantityRequired;
            let stockBadge = '';
            if (isStockOk) {
                stockBadge = `<span class="badge badge-stock-ok"><i class="fa-solid fa-check"></i> Kho: ${stock}</span>`;
            } else {
                stockBadge = `<span class="badge badge-stock-low"><i class="fa-solid fa-xmark"></i> Thiếu: ${stock}</span>`;
                canApprove = false;
                warningMessages.push(`Thiếu hàng: <strong>${p.partName || p.partNumber}</strong> (Cần ${p.quantityRequired}, Kho ${stock})`);
            }

            // 2. Check Bảo hành
            const warrantyCheck = analyzeWarranty(vehicle, info);
            if (!warrantyCheck.isValid) {
                canApprove = false;
                warningMessages.push(`Hết bảo hành: <strong>${p.partName || p.partNumber}</strong> - ${warrantyCheck.reason}`);
            }

            return `
            <li class="list-group-item">
                <div class="d-flex justify-content-between align-items-start">
                    <div>
                        <div class="fw-bold">${p.partName || p.partNumber}</div>
                        <small class="text-muted">Mã: ${p.partNumber} | SL: ${p.quantityRequired}</small>
                    </div>
                    <div class="text-end">
                        <div class="mb-1">${stockBadge}</div>
                        <div>${warrantyCheck.badge}</div>
                    </div>
                </div>
                ${!warrantyCheck.isValid ? `<div class="small text-danger mt-1 bg-light p-1 rounded border border-danger"><i class="fa-solid fa-triangle-exclamation"></i> ${warrantyCheck.reason}</div>` : ''}
                <div class="text-end small text-muted border-top mt-2 pt-1">
                    Giá: ${price.toLocaleString()} đ x ${p.quantityRequired} = <strong>${lineTotal.toLocaleString()} đ</strong>
                </div>
            </li>
            `;
        }).join('');
    }

    // C. Tạo thông báo lỗi (Alert)
    let alertHtml = '';
    if (warningMessages.length > 0) {
        alertHtml = `
            <div class="alert alert-danger small mb-3">
                <div class="fw-bold mb-1"><i class="fa-solid fa-ban"></i> KHÔNG THỂ PHÊ DUYỆT:</div>
                <ul class="mb-0 ps-3">
                    ${warningMessages.map(msg => `<li>${msg}</li>`).join('')}
                </ul>
            </div>
        `;
    }

    // D. Cập nhật trạng thái nút (Enable/Disable nút ở Footer)
    const btnApprove = document.getElementById('btn-approve-action');
    if (btnApprove) {
        btnApprove.disabled = !canApprove;
    }

    // E. Render Giao diện (3 Cột - ĐÃ XÓA NÚT DƯ THỪA)
    body.innerHTML = `
        <div class="row">
            <div class="col-md-7 border-end">
                <div class="d-flex gap-3 mb-3 p-3 bg-light rounded border">
                    <div><i class="fa-solid fa-car"></i> <strong>${vehicle.model}</strong></div>
                    <div><i class="fa-solid fa-calendar"></i> ${new Date(vehicle.warrantyStartDate).toLocaleDateString('vi-VN')}</div>
                    <div><i class="fa-solid fa-road"></i> ${vehicle.currentOdometer?.toLocaleString()} Km</div>
                </div>
                
                <h6 class="text-secondary fw-bold small">MÔ TẢ LỖI TỪ SC</h6>
                <p class="p-2 border rounded mb-4 bg-white" style="min-height: 60px;">${claim.description}</p>
                
                <h6 class="text-secondary fw-bold small d-flex justify-content-between align-items-center border-bottom pb-2">
                    <span>PHỤ TÙNG YÊU CẦU</span>
                    <span>Tổng dự kiến: <span class="text-primary fs-6 fw-bold">${totalCost.toLocaleString()} đ</span></span>
                </h6>
                <ul class="list-group list-group-flush" style="max-height: 350px; overflow-y: auto;">
                    ${partsHtml}
                </ul>
            </div>
            
            <div class="col-md-5 ps-4">
                <h6 class="text-primary fw-bold mb-3"><i class="fa-solid fa-gavel"></i> QUYẾT ĐỊNH</h6>
                
                <div class="mb-3">
                    <label class="form-label fw-bold small">1. Chỉ định Kỹ thuật viên (Bắt buộc)</label>
                    <select class="form-select" id="review-technician">
                        ${techOptions}
                    </select>
                </div>
                
                <div class="mb-3">
                    <label class="form-label fw-bold small">2. Ghi chú</label>
                    <textarea class="form-control" id="review-notes" rows="5" placeholder="Nhập lý do..."></textarea>
                </div>

                ${alertHtml}

                </div>
        </div>
    `;

    // (Không cần gán onclick lại ở đây nữa vì initReviewModal đã gán 1 lần cho nút ở Footer rồi)
}

function analyzeWarranty(vehicle, partInfo) {
    if (!partInfo.warrantyDurationMonths && !partInfo.warrantyMileageLimit) {
        return { isValid: true, badge: '<span class="badge bg-secondary">Không áp dụng CS</span>', reason: '' };
    }

    const startDate = new Date(vehicle.warrantyStartDate);
    const now = new Date();
    const monthsUsed = (now.getFullYear() - startDate.getFullYear()) * 12 + (now.getMonth() - startDate.getMonth());

    if (partInfo.warrantyDurationMonths > 0 && monthsUsed > partInfo.warrantyDurationMonths) {
        return {
            isValid: false,
            badge: '<span class="badge badge-policy-fail">Hết BH (Thời gian)</span>',
            reason: `Xe đã dùng ${monthsUsed} tháng (Quy định: ${partInfo.warrantyDurationMonths} tháng)`
        };
    }

    if (partInfo.warrantyMileageLimit > 0 && vehicle.currentOdometer > partInfo.warrantyMileageLimit) {
        return {
            isValid: false,
            badge: '<span class="badge badge-policy-fail">Hết BH (Odo)</span>',
            reason: `Xe đã chạy ${vehicle.currentOdometer.toLocaleString()} Km (Quy định: ${partInfo.warrantyMileageLimit.toLocaleString()} Km)`
        };
    }

    return { isValid: true, badge: '<span class="badge badge-policy-ok">Hợp lệ</span>', reason: '' };
}

async function handleApproveClick() {
    const techId = document.getElementById('review-technician').value;
    const notes = document.getElementById('review-notes').value;

    if (!techId) {
        alert("Vui lòng chọn Kỹ thuật viên để thực hiện sửa chữa.");
        return;
    }
    if (!confirm("Xác nhận PHÊ DUYỆT yêu cầu này?")) return;

    try {
        await approveClaim(currentClaimId, notes, techId);
        alert("Đã phê duyệt thành công!");
        reviewModalInstance.hide();
        loadApprovalQueue();
        loadEvmStats();
    } catch (e) {
        alert("Lỗi khi duyệt: " + e.message);
    }
}

async function handleRejectClick() {
    const notes = document.getElementById('review-notes').value;
    if (!notes) {
        alert("Vui lòng nhập lý do từ chối vào ô Ghi chú.");
        return;
    }
    if (!confirm("Xác nhận TỪ CHỐI yêu cầu này?")) return;

    try {
        await rejectClaim(currentClaimId, notes);
        alert("Đã từ chối yêu cầu!");
        reviewModalInstance.hide();
        loadApprovalQueue();
        loadEvmStats();
    } catch (e) {
        alert("Lỗi khi từ chối: " + e.message);
    }
}