// src/services/partService.js
import { api } from './apiClient.js';

/**
 * Lấy danh sách phụ tùng (có phân trang và tìm kiếm)
 * API: GET /api/v1/parts?name=...&page=...
 * (part-service của bạn đã có API này)
 */
export const searchParts = (params) => {
    // params là object, ví dụ: { name: 'motor', page: 0, size: 5 }
    const query = new URLSearchParams(params).toString();
    return api.get(`/parts?${query}`);
};

/**
 * Lấy chi tiết 1 phụ tùng (nếu cần)
 * API: GET /api/v1/parts/{id}
 */
export const getPartById = (id) => {
    return api.get(`/parts/${id}`);
};