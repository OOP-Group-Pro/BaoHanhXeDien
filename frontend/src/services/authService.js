// src/services/authService.js
import { api } from './apiClient.js';
import { saveToken, saveUser, clearToken, clearUser } from '../utils/storage.js';
import { jwtDecode } from 'jwt-decode'; // ⬅️ Thư viện đã cài

/**
 * Gọi API Login
 */
export async function login(username, password) {
    try {
        // 1. Gọi API (apiClient sẽ tự xử lý POST)
        const data = await api.post('/auth/login', { username, password });

        // 2. data lúc này là { token: "..." }
        const token = data.token;
        if (!token) {
            throw new Error('Không nhận được token từ server');
        }

        // 3. Lưu token
        saveToken(token);

        // 4. Giải mã token để lưu thông tin user
        const decodedToken = jwtDecode(token);
        const userInfo = {
            id: decodedToken.sub,
            roles: decodedToken.roles,
            centerId: decodedToken.centerId,
            username: username // (Tạm thời lưu username)
        };
        saveUser(userInfo); // ⬅️ Lưu vào localStorage

        return userInfo;

    } catch (error) {
        console.error('Lỗi đăng nhập:', error);
        throw error; // Ném lỗi ra để main.js bắt
    }
}

/**
 * Xử lý Đăng xuất
 */
export function logout() {
    clearToken();
    clearUser();
    window.location.href = '/index.html';
}