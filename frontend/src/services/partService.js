import { api } from './apiClient.js';

// --- NHÓM TRA CỨU & TÌM KIẾM ---

/**
 * Tìm kiếm phụ tùng (cho trang Tạo Claim)
 * API: GET /api/v1/parts?name=...
 */
export const searchParts = (params) => {
    const query = new URLSearchParams(params).toString();
    return api.get(`/parts?${query}`);
};

/**
 * Lấy chi tiết 1 phụ tùng
 * API: GET /api/v1/parts/{id}
 */
export const getPartById = (id) => {
    return api.get(`/parts/${id}`);
};

/**
 * ✅ HÀM QUAN TRỌNG CHO EVM DASHBOARD
 * Lấy chi tiết nhiều phụ tùng (kèm Tồn kho & Policy)
 * API: POST /api/v1/parts/by-numbers
 * Input: List<String> (danh sách partType/partNumber)
 * Output: Map<String, PartResponse>
 */
export const getPartsDetails = (partNumbers) => {
    return api.post('/parts/by-numbers', partNumbers);
};


// --- NHÓM QUẢN LÝ (ADMIN/INVENTORY) ---

export const getParts = (page = 0, size = 10) =>
    api.get(`/parts?page=${page}&size=${size}`);

export const getAllInventory = () =>
    api.get('/inventory/all-stock');

export const getAllocationStatusForClaim = (claimId) =>
    api.get(`/allocations/status-by-claim/${claimId}`);

export const createPart = (partData) =>
    api.post('/parts', partData);

export const updateInventoryQuantity = (inventoryId, newQuantity) =>
    api.patch(`/inventory/${inventoryId}/quantity`, { quantity: newQuantity });

export const requestAllocationForClaim = (allocationData) =>
    api.post('/parts/allocate-claim', allocationData);

export const decrementStock = (decrementData) =>
    api.post('/inventory/decrement', decrementData);