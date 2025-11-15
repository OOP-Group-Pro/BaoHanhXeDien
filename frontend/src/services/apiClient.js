// src/services/apiClient.js
import { getToken } from '../utils/storage.js';
import { logout } from '../utils/auth.js'; // ⬅️ Sửa: import từ auth.js

const API_BASE_URL = '/api/v1';

async function apiClient(endpoint, options = {}) {
    const token = getToken();

    const defaultHeaders = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    if (token) {
        defaultHeaders['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers: defaultHeaders,
    };

    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

    // ⬇️ SỬA LỖI LOGIC NẰM Ở ĐÂY ⬇️

    // BƯỚC 1: Xử lý lỗi 401 (Unauthorized) NGAY LẬP TỨC
    if (response.status === 401) {
        alert('Phiên đăng nhập hết hạn hoặc không hợp lệ.');
        logout(); // Tự động đá ra trang login
        // Ném ra lỗi để ngăn code chạy tiếp
        throw new Error('Unauthorized');
    }

    // BƯỚC 2: Xử lý các lỗi khác (400, 403, 500...)
    if (!response.ok) {
        const errorData = await response.json(); // Lỗi 403, 500 thường có body JSON
        const message = errorData.message || 'Lỗi API';
        console.error('API Error:', message, 'Path:', errorData.path);
        throw new Error(message);
    }

    // BƯỚC 3: Xử lý 200 OK
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        return response.json();
    }

    // (Xử lý POST /claims trả về ID dạng text)
    return response.text();
}

// ⬇️ Mọi người sẽ dùng cái này
export const api = {
    get: (endpoint) => apiClient(endpoint),
    post: (endpoint, body) => apiClient(endpoint, { method: 'POST', body: JSON.stringify(body) }),
    put: (endpoint, body) => apiClient(endpoint, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (endpoint) => apiClient(endpoint, { method: 'DELETE' }),
};