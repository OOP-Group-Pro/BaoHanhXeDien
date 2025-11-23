import { getClaims, getClaimDetails, approveClaim, rejectClaim } from '../services/warrantyService.js';
import { getUsersByRole } from '../services/userService.js';
import { getVehicleByVin } from '../services/vehicleService.js';
import { getPartsDetails } from '../services/partService.js'; // ⬅️ Đã có hàm này từ bước 1

let reviewModalInstance = null;
let currentClaimId = null;

// 1. STATE BỘ LỌC
let filterState = {
    page: 0,
    size: 10,
    status: 'WAITING_APPROVAL', // Mặc định
    vin: '',
    claimCode: ''
};

export function setupEvmDashboard() {
    console.log("LOG: Init Dashboard EVM Logic...");
    loadEvmStats();

    // Gắn sự kiện cho bộ lọc
    document.getElementById('btn-apply-filter')?.addEventListener('click', handleFilterApply);
    document.getElementById('btn-clear-filter')?.addEventListener('click', handleFilterClear);

    // Load lần đầu
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

// 2. HÀM LOAD DANH SÁCH (NÂNG CẤP)
async function loadApprovalQueue() {
    const tbody = document.getElementById('approval-table-body');
    const paginationEl = document.getElementById('pagination-container'); // Nếu có phân trang

    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="7" class="text-center"><div class="spinner-border spinner-border-sm text-primary"></div> Đang tải...</td></tr>';

    try {
        // ⬇️ BƯỚC SỬA QUAN TRỌNG: LÀM SẠCH THAM SỐ ⬇️
        // Tạo một object params mới, chỉ chứa những gì có giá trị
        const cleanParams = {
            page: filterState.page,
            size: filterState.size
        };

        // Chỉ thêm status nếu khác rỗng (Tất cả)
        if (filterState.status) {
            cleanParams.status = filterState.status;
        }

        // Chỉ thêm VIN nếu người dùng thực sự đã nhập
        if (filterState.vin && filterState.vin.trim() !== '') {
            cleanParams.vin = filterState.vin.trim();
        }

        // Chỉ thêm ClaimCode nếu người dùng thực sự đã nhập
        if (filterState.claimCode && filterState.claimCode.trim() !== '') {
            cleanParams.claimCode = filterState.claimCode.trim();
        }

        // Gọi API với bộ tham số đã làm sạch
        const data = await getClaims(cleanParams);

        tbody.innerHTML = '';

        if (data.empty) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">Không tìm thấy dữ liệu phù hợp.</td></tr>';
            paginationEl.innerHTML = ''; // Xóa phân trang
            return;
        }

        // Render Bảng
        data.content.forEach(claim => {
            const desc = claim.description && claim.description.length > 30
                ? claim.description.substring(0, 30) + '...'
                : (claim.description || '');

            // Badge trạng thái
            let statusClass = 'bg-secondary';
            if (claim.currentStatus === 'APPROVED') statusClass = 'bg-success';
            if (claim.currentStatus === 'WAITING_APPROVAL') statusClass = 'bg-warning text-dark';
            if (claim.currentStatus === 'REJECTED') statusClass = 'bg-danger';

            tbody.innerHTML += `
                <tr>
                    <td class="ps-4 fw-bold text-primary">${claim.claimCode}</td>
                    <td>${claim.vin}</td>
                    <td>${desc}</td>
                    <td>${new Date(claim.dateCreated).toLocaleDateString('vi-VN')}</td>
                    <td>SC Staff</td> <td><span class="badge ${statusClass}">${claim.currentStatus}</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary btn-review" 
                                data-id="${claim.id}" 
                                data-code="${claim.claimCode}">
                            <i class="fa-solid fa-eye"></i> Xem
                        </button>
                    </td>
                </tr>
            `;
        });

        // Render Phân trang
        renderPagination(data, paginationEl);

        // Gắn lại sự kiện click
        document.querySelectorAll('.btn-review').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const btn = e.currentTarget; // Sửa lỗi logic cũ
                openReviewModal(btn.dataset.id, btn.dataset.code);
            });
        });

    } catch (e) {
        console.error(e);
        tbody.innerHTML = `<tr><td colspan="7" class="text-danger text-center">Lỗi: ${e.message}</td></tr>`;
    }
}

// 3. HÀM RENDER PHÂN TRANG
function renderPagination(pageData, container) {
    const { number, totalPages, first, last } = pageData;

    let html = `
        <div class="text-muted small">Trang ${number + 1} / ${totalPages}</div>
        <nav>
            <ul class="pagination pagination-sm mb-0">
                <li class="page-item ${first ? 'disabled' : ''}">
                    <button class="page-link" onclick="changePage(${number - 1})">Trước</button>
                </li>
                <li class="page-item ${last ? 'disabled' : ''}">
                    <button class="page-link" onclick="changePage(${number + 1})">Sau</button>
                </li>
            </ul>
        </nav>
    `;
    container.innerHTML = html;
}

// 4. HÀM XỬ LÝ SỰ KIỆN
function handleFilterApply() {
    filterState.claimCode = document.getElementById('filter-code').value.trim();
    filterState.vin = document.getElementById('filter-vin').value.trim();
    filterState.status = document.getElementById('filter-status').value;
    filterState.page = 0; // Reset về trang đầu
    loadApprovalQueue();
}

function handleFilterClear() {
    document.getElementById('filter-code').value = '';
    document.getElementById('filter-vin').value = '';
    document.getElementById('filter-status').value = 'WAITING_APPROVAL'; // Reset về mặc định

    handleFilterApply();
}

// Export hàm global để HTML gọi được (cho nút phân trang)
window.changePage = (newPage) => {
    filterState.page = newPage;
    loadApprovalQueue();
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
    // --- LOG DEBUG START ---
    console.group("🔍 DEBUG: renderSmartReview");
    console.log("1. Dữ liệu Claim:", claim);
    console.log("2. Dữ liệu Xe (Vehicle):", vehicle);
    console.log("3. Dữ liệu PartsMap (Từ API):", partsMap);
    console.log("4. Danh sách KTV:", technicians);
    // --- LOG DEBUG END ---

    const body = document.getElementById('modal-body-content');

    if (!partsMap) {
        console.error("😡 LỖI: partsMap bị null hoặc undefined!");
    }

    // A. Render Dropdown KTV
    let techOptions = '<option value="">-- Chọn Kỹ thuật viên --</option>';
    if (technicians && technicians.length > 0) {
        techOptions += technicians.map(t => `<option value="${t.userId}">${t.fullName}</option>`).join('');
    }

    // B. Render Danh sách Phụ tùng & Logic kiểm tra
    let totalCost = 0;
    let canApprove = true;
    let warningMessages = [];

    let partsHtml = '';
    if (!claim.partList || claim.partList.length === 0) {
        partsHtml = '<li class="list-group-item text-muted">Không có phụ tùng yêu cầu</li>';
    } else {
        partsHtml = claim.partList.map((p, index) => {
            // --- LOG DEBUG VÒNG LẶP ---
            console.groupCollapsed(`🛠️ Kiểm tra Part #${index + 1}: ${p.partName || 'No Name'}`);
            console.log("🔹 Dữ liệu Part trong Claim (p):", p);

            // Kiểm tra xem key dùng để map là gì
            // (Lưu ý: DTO Backend trả về 'partNumber', code cũ bạn dùng 'partType', hãy check log xem cái nào có giá trị)
            const lookupKey = p.partType || p.partNumber;
            console.log(`🔹 Key dùng để tra cứu trong Map: "${lookupKey}"`);
            console.log(`🔹 Giá trị p.partType:`, p.partType);
            console.log(`🔹 Giá trị p.partNumber:`, p.partNumber);

            const info = partsMap[lookupKey] || {};

            if (Object.keys(info).length === 0) {
                console.warn(`⚠️ CẢNH BÁO: Không tìm thấy thông tin cho key "${lookupKey}" trong partsMap.`);
            } else {
                console.log("✅ Tìm thấy thông tin Part (Info):", info);
                console.log(`   - Tồn kho (inventoryQuantity): ${info.inventoryQuantity}`);
                console.log(`   - Giá (price): ${info.price}`);
                console.log(`   - BH Tháng (warrantyDurationMonths): ${info.warrantyDurationMonths}`);
                console.log(`   - BH Km (warrantyMileageLimit): ${info.warrantyMileageLimit}`);
            }
            // --- LOG DEBUG END ---

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
            console.log("🛡️ Kết quả Check Bảo hành:", warrantyCheck); // Log kết quả check bảo hành

            if (!warrantyCheck.isValid) {
                canApprove = false;
                warningMessages.push(`Hết bảo hành: <strong>${p.partName || p.partNumber}</strong> - ${warrantyCheck.reason}`);
            }

            console.groupEnd(); // Kết thúc nhóm log cho part này

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

    console.groupEnd(); // Kết thúc nhóm log tổng

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

    // E. Render Giao diện (3 Cột)
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