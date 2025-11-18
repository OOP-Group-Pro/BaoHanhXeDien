// src/services/userService.js
import { api } from './apiClient.js'; // api client có sẵn auth

// --- CÁC HÀM CỦA THÙY ---

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
    return api.post(`/users/admin/${roleName}`, userData); // ⬅️ Sửa: Dùng API đúng của bạn
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

// (Bạn có thể thêm các hàm cho NewStaffRequest ở đây nếu cần)

