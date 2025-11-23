// src/services/warrantyService.js
import { api } from './apiClient.js'; // Import hàm fetch đã có auth

/**
 * Lấy danh sách claims
 * @param {object} params - Ví dụ: { page: 0, size: 10, status: 'WAITING_APPROVAL' }
 */
export const getClaims = (params) => {
    const query = new URLSearchParams(params).toString();
    return api.get(`/claims?${query}`);
};

/**
 * Tạo claim mới
 * @param {object} claimData - Ví dụ: { vin: "...", description: "...", ... }
 */
export const createClaim = (claimData) => {
    // API này (POST /claims) trả về ID (dạng text), apiClient.js đã xử lý
    return api.post('/claims', claimData);
};

// --- BỔ SUNG HÀM MỚI ---

/**
 * Từ chối một Claim
 * API: PUT /api/v1/claims/{claimId}/reject?reason=...
 */
export const rejectClaim = (claimId, reason) => {
    // API này không cần body, chỉ cần URL
    return api.put(`/claims/${claimId}/reject?reason=${encodeURIComponent(reason)}`);
};

/**
 * Phê duyệt một Claim
 * API: PUT /api/v1/claims/{claimId}/approve?approvalNotes=...
 */
export const approveClaim = (claimId, approvalNotes = '') => {
    return api.put(`/claims/${claimId}/approve?approvalNotes=${encodeURIComponent(approvalNotes)}`);
};

/**
 * Lấy chi tiết một Claim
 * API: GET /api/v1/claims/{claimId}
 */
export const getClaimDetails = (claimId) => {
    return api.get(`/claims/${claimId}`);
};

/**
 * Lấy lịch sử một Claim
 * API: GET /api/v1/claims/{claimId}/history
 */
export const getClaimHistory = (claimId) => {
    return api.get(`/claims/${claimId}/history`);
};

/**
 * Cập nhật kết quả sửa chữa (dành cho Kỹ thuật viên)
 * API: PUT /api/v1/claims/{claimId}/repair-result
 * @param {string} claimId - ID của claim
 * @param {object} repairData - Dữ liệu DTO (ClaimRepairResultDto)
 */
export const updateRepairResult = (claimId, repairData) => {
    // API này cần gửi kèm 1 body (khác với approve/reject)
    return api.put(`/claims/${claimId}/repair-result`, repairData);

};