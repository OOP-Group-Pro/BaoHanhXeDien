// src/services/warrantyService.js
import { api } from './apiClient.js'; // Import hàm fetch đã có auth

/**
 * Lấy danh sách claims (đã test 200 OK với Postman)
 * @param {object} params - Ví dụ: { page: 0, size: 10, status: 'WAITING_APPROVAL' }
 */
export const getClaims = (params) => {
    const query = new URLSearchParams(params).toString();
    return api.get(`/claims?${query}`);
};

/**
 * Tạo claim mới (đã test 201 OK với Postman)
 * @param {object} claimData - Ví dụ: { vin: "...", description: "...", ... }
 */
export const createClaim = (claimData) => {
    // Lưu ý: Postman trả về ID (số "3"), không phải JSON
    // apiClient của chúng ta có thể cần sửa để xử lý text()
    // Tạm thời cứ dùng .post()
    return api.post('/claims', claimData);
};