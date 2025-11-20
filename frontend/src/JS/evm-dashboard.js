// src/JS/evm-dashboard.js

import { getClaims, getClaimDetails, getClaimHistory, approveClaim, rejectClaim } from '../services/warrantyService.js';
import { getUsersByRole } from '../services/userService.js';

let reviewModalInstance = null;
let currentClaimId = null; // Lưu ID claim đang xem

/**
 * Hàm khởi tạo (được gọi bởi evmStaff.js)
 */
export function setupEvmDashboard() {
  console.log("LOG: Init Dashboard EVM Logic...");

  loadEvmStats();
  loadApprovalQueue();
  initReviewModal();
}

// --- 1. Tải thống kê & Danh sách ---

async function loadEvmStats() {
  try {
    const [pending, approved] = await Promise.all([
      getClaims({ status: 'WAITING_APPROVAL', size: 1, page: 0 }),
      getClaims({ status: 'APPROVED', size: 1, page: 0 })
    ]);

    const kpiPending = document.getElementById('kpi-pending');
    const kpiApproved = document.getElementById('kpi-approved');

    if(kpiPending) kpiPending.textContent = pending.totalElements;
    if(kpiApproved) kpiApproved.textContent = approved.totalElements;
  } catch (e) { console.error("Lỗi KPI:", e); }
}

async function loadApprovalQueue() {
  const tbody = document.getElementById('approval-table-body');
  if (!tbody) return;

  tbody.innerHTML = '<tr><td colspan="5" class="text-center">Đang tải...</td></tr>';

  try {
    // Lấy 20 claim đang chờ duyệt
    const data = await getClaims({ status: 'WAITING_APPROVAL', size: 20, page: 0 });
    tbody.innerHTML = '';

    if (data.empty) {
      tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">Không có yêu cầu nào cần duyệt.</td></tr>';
      return;
    }

    data.content.forEach(claim => {
      // Cắt ngắn mô tả
      const desc = claim.description && claim.description.length > 50
          ? claim.description.substring(0, 50) + '...'
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
                            <i class="bi bi-search"></i> Thẩm định
                        </button>
                    </td>
                </tr>
            `;
    });

    // Gắn sự kiện click cho các nút "Thẩm định"
    document.querySelectorAll('.btn-review').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const id = e.currentTarget.dataset.id;
        const code = e.currentTarget.dataset.code;
        openReviewModal(id, code);
      });
    });

  } catch (e) {
    console.error(e);
    tbody.innerHTML = `<tr><td colspan="5" class="text-danger text-center">Lỗi tải dữ liệu: ${e.message}</td></tr>`;
  }
}

// --- 2. Logic Modal Thẩm Định ---

function initReviewModal() {
  const modalEl = document.getElementById('reviewModal');
  if (modalEl) reviewModalInstance = new bootstrap.Modal(modalEl);

  // Gắn sự kiện cho 2 nút Quyết định
  document.getElementById('btn-approve-action')?.addEventListener('click', handleApproveClick);
  document.getElementById('btn-reject-action')?.addEventListener('click', handleRejectClick);
}

async function openReviewModal(id, code) {
  currentClaimId = id;
  document.getElementById('modal-claim-code').textContent = code;
  reviewModalInstance.show();

  const body = document.getElementById('modal-body-content');
  body.innerHTML = '<div class="text-center p-5"><div class="spinner-border text-primary"></div><p class="mt-2">Đang tải dữ liệu chi tiết...</p></div>';

  try {
    // Gọi song song: Chi tiết Claim + Danh sách KTV (để gán)
    const [details, technicians] = await Promise.all([
      getClaimDetails(id), // Lưu ý: API getClaimDetails nhận ID hoặc Code tuỳ bạn cài đặt ở warrantyService.js
      getUsersByRole('SC_TECHNICIAN') // Lấy list KTV để EVM chọn
    ]);

    renderReviewModalContent(details, technicians);
  } catch (e) {
    console.error(e);
    body.innerHTML = `<div class="alert alert-danger">Không thể tải chi tiết: ${e.message}</div>`;
  }
}

function renderReviewModalContent(claim, technicians) {
  const body = document.getElementById('modal-body-content');

  // Tạo Options cho Dropdown KTV
  let techOptions = '<option value="">-- Chọn Kỹ thuật viên (Bắt buộc khi duyệt) --</option>';
  if (technicians && technicians.length > 0) {
    techOptions += technicians.map(t => `<option value="${t.userId}">${t.fullName}</option>`).join('');
  }

  // Tạo HTML danh sách phụ tùng
  let partsHtml = '<li class="list-group-item text-muted">Không có phụ tùng yêu cầu</li>';
  if (claim.partList && claim.partList.length > 0) {
    partsHtml = claim.partList.map(p => `
            <li class="list-group-item d-flex justify-content-between align-items-center">
                <div>
                    <div class="fw-bold">${p.partName || p.partNumber}</div>
                    <small class="text-muted">Mã: ${p.partNumber}</small>
                </div>
                <span class="badge bg-secondary rounded-pill">SL: ${p.quantityRequired}</span>
            </li>
        `).join('');
  }

  // Render HTML (Chia 2 cột)
  body.innerHTML = `
        <div class="row">
            <div class="col-md-6 border-end">
                <h6 class="text-uppercase text-muted mb-3">Thông tin Yêu cầu</h6>
                <div class="mb-2"><strong>VIN:</strong> ${claim.vin}</div>
                <div class="mb-2"><strong>Khách hàng:</strong> ${claim.customerName || 'N/A'}</div>
                <div class="mb-3">
                    <strong>Mô tả sự cố:</strong>
                    <div class="bg-light p-2 rounded mt-1 border">${claim.description}</div>
                </div>
                
                <h6 class="text-uppercase text-muted mt-4 mb-2">Phụ tùng Yêu cầu</h6>
                <ul class="list-group mb-3">
                    ${partsHtml}
                </ul>
            </div>
            
            <div class="col-md-6 ps-4">
                <h6 class="text-uppercase text-primary mb-3">Quyết định Thẩm định</h6>
                
                <div class="mb-3">
                    <label class="form-label fw-bold">Chỉ định Kỹ thuật viên (Sửa chữa)</label>
                    <select class="form-select" id="review-technician">
                        ${techOptions}
                    </select>
                    <div class="form-text text-muted">Chọn KTV sẽ thực hiện thay thế/sửa chữa.</div>
                </div>
                
                <div class="mb-3">
                    <label class="form-label fw-bold">Ghi chú Thẩm định</label>
                    <textarea class="form-control" id="review-notes" rows="4" placeholder="Nhập lý do duyệt/từ chối..."></textarea>
                </div>
            </div>
        </div>
    `;
}

// --- 3. Xử lý Hành động Duyệt / Từ chối ---

async function handleApproveClick() {
  const techId = document.getElementById('review-technician').value;
  const notes = document.getElementById('review-notes').value;

  if (!techId) {
    alert("Vui lòng chọn Kỹ thuật viên để thực hiện sửa chữa.");
    return;
  }

  if (!confirm("Xác nhận PHÊ DUYỆT yêu cầu này?")) return;

  try {
    // Gọi API (Cần đảm bảo warrantyService.js có hàm này và đúng tham số)
    await approveClaim(currentClaimId, notes, techId);

    alert("Đã phê duyệt thành công!");
    reviewModalInstance.hide();
    loadApprovalQueue(); // Tải lại danh sách
    loadEvmStats();      // Tải lại KPI
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