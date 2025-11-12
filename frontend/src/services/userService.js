// src/services/userService.js
import { api } from './apiClient.js'; // api client có sẵn auth

/**
 * Lấy danh sách tất cả user
 */
export const getAllUsers = () => {
    return api.get('/users');
};

/**
 * Lấy user theo ID
 * @param {number} userId
 */
export const getUserById = (userId) => {
    return api.get(`/users/${userId}`);
};

/**
 * Tạo user mới (chỉ admin)
 * @param {object} userData - ví dụ: { username, email, phone, password, status }
 * @param {string} roleName - ví dụ: "ROLE_ADMIN" hoặc "ROLE_USER"
 */
export const createUser = (userData, roleName) => {
    return api.post('/users', { ...userData, roleName });
};

/**
 * Cập nhật user (chỉ admin)
 * @param {number} userId
 * @param {object} userData - ví dụ: { username, email, phone, status }
 */
export const updateUser = (userId, userData) => {
    return api.put(`/users/${userId}`, userData);
};

/**
 * Xóa user (chỉ admin)
 * @param {number} userId
 */
export const deleteUser = (userId) => {
    return api.delete(`/users/${userId}`);
};

/**
 * Lấy thông tin cơ bản của user
 * @param {number} userId
 */
export const getBasicUserDetails = (userId) => {
    return api.get(`/users/${userId}/basic`);
};
