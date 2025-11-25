// src/services/userService.js
import { api } from './apiClient.js'; // api client có sẵn auth

// --- CÁC HÀM CỦA THÙY ---

/**
 * Lấy thông tin cá nhân (Tự động lấy theo Token)
 * API: GET /api/v1/users/me
 */
export const getMyProfile = () => {
    // Gọi API /me thay vì truyền ID (để Backend tự lấy ID từ Token -> Bảo mật hơn)
    return api.get('/users/me');
};

/**
 * Cập nhật thông tin cá nhân
 * API: PUT /api/v1/users/me
 */
export const updateMyProfile = (data) => {
    return api.put('/users/me', data);
};

/**
 * Lấy danh sách tất cả user (cho Admin)
 */
export const getAllUsers = () => {
    return api.get('/users/admin'); // ⬅️ Sửa: Dùng API đúng của bạn
};

/**
 * Lấy user (full) theo ID (cho Admin)
 * @param {number} userId
 */
export const getUserFullById = (userId) => {
    return api.get(`/users/admin/${userId}`); // ⬅️ Sửa: Dùng API đúng của bạn
};

/**
 * Tạo user mới (chỉ admin)
 * @param {object} userData - ví dụ: { username, email, ... }
 * @param {string} roleName - ví dụ: "SC_STAFF"
 */
export const createUser = (userData, roleName) => {
    return api.post(`/users/admin/${roleName}`, userData);
};

/**
 * Cập nhật user (chỉ admin)
 * @param {number} userId
 * @param {object} userData - ví dụ: { username, email, ... }
 */
export const updateUser = (userId, userData) => {
    return api.put(`/users/admin/${userId}`, userData); // ⬅️ Sửa: Dùng API đúng của bạn
};

/**
 * Xóa user (chỉ admin)
 * @param {number} userId
 */
export const deleteUser = (userId) => {
    return api.delete(`/users/admin/${userId}`); // ⬅️ Sửa: Dùng API đúng của bạn
};

// --- CÁC HÀM CỦA ĐẠT (VÀ CÁC SERVICE KHÁC) ---

/**
 * Lấy thông tin cơ bản (DTO) của user (cho Feign)
 * @param {number} userId
 */
export const getBasicUserDetails = (userId) => {
    // ⬅️ Sửa: API này của bạn là /users/{id}, không phải /users/{id}/basic
    return api.get(`/users/${userId}`);
};

/**
 * Lấy danh sách người dùng theo vai trò (ví dụ: Kỹ thuật viên)
 * (Dùng API mới @GetMapping của bạn)
 * @param {string} roleName (ví dụ: 'ROLE_TECHNICIAN')
 */
export async function getUsersByRole(roleName) {
    const params = new URLSearchParams();
    params.append('roleName', roleName);
    return api.get(`/users?${params.toString()}`);
}
// 🟢 Manager tạo phiếu mới
/**
 * Manager tạo phiếu mới
 * @param {object} requestData - { fullName, username, email, phone, roleName }
 */
export const createStaffRequest = (requestData) => {
    // Chuyển roleName thành proposedRoles array để backend map đúng entity
    const payload = {
        ...requestData,
        proposedRoles: [requestData.roleName], // backend NewStaffRequest mong Set<Role>
    };
    delete payload.roleName; // xoá roleName dư thừa
    return api.post('/staff-requests/create', payload);
};


// 🟢 Manager lấy danh sách phiếu của chính mình
export async function getMyStaffRequests(createdBy) {
    const params = new URLSearchParams();
    if (createdBy) params.append('createdBy', createdBy); // nếu muốn lọc theo creator
    const queryString = params.toString() ? `?${params.toString()}` : '';
    return api.get(`/staff-requests/my${queryString}`);
}


// (Bạn có thể thêm các hàm cho NewStaffRequest ở đây nếu cần)

////////////////////////////////INVENTORY///////////////////////////////////////

// Lấy toàn bộ tồn kho
export const getAllInventory = () => api.get('/inventory/all-stock');

// Thêm hoặc cập nhật stock
export const addOrUpdateStock = (stockData) => api.post('/inventory/stock', stockData);

// Lấy inventory theo partId
export const getInventoryByPartId = (partId) => api.get(`/inventory/part/${partId}`);

// Lấy inventory theo location
export const getInventoryByLocation = (location) => api.get('/inventory/location', { params: { location } });

// Cập nhật trạng thái inventory
export const updateInventoryStatus = (inventoryId, status) => api.patch(`/inventory/${inventoryId}/status`, { status });

// Cập nhật số lượng tồn kho
export const updateInventoryQuantity = (inventoryId, newQuantity) =>
    api.patch(`/inventory/${inventoryId}/quantity`, { quantity: newQuantity });