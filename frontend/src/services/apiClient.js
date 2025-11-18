// src/services/apiClient.js
import { getToken } from '../utils/storage.js';
import { logout } from '../utils/auth.js';

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

    // Xử lý FormData (xóa Content-Type để browser tự điền boundary)
    if (options.body && options.body instanceof FormData) {
        delete config.headers['Content-Type'];
    } else if (options.body) {
        config.body = JSON.stringify(options.body);
    }

    // GỌI API
    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

    // -------------------------------------------------------
    // ⬇️ BƯỚC 1: KIỂM TRA LỖI TRƯỚC (QUAN TRỌNG NHẤT) ⬇️
    // -------------------------------------------------------

    // 1a. Xử lý 401 (Hết phiên)
    if (response.status === 401) {
        logout();
        throw new Error('Phiên đăng nhập hết hạn.');
    }

    // 1b. Xử lý các lỗi khác (400, 403, 404, 500...)
    if (!response.ok) {
        // Cố gắng đọc lỗi từ JSON server trả về
        let errorMessage = 'Lỗi API';
        try {
            const errorData = await response.json();
            errorMessage = errorData.message || errorData.error || errorMessage;
        } catch (e) {
            // Nếu server không trả JSON mà trả text (hoặc HTML lỗi)
            errorMessage = await response.text();
        }

        console.error('API Error:', errorMessage);
        throw new Error(errorMessage);
    }

    // -------------------------------------------------------
    // ⬇️ BƯỚC 2: XỬ LÝ DỮ LIỆU THÀNH CÔNG (200 OK) ⬇️
    // -------------------------------------------------------

    // Nếu client yêu cầu Blob (File/Ảnh)
    if (options.responseType === 'blob') {
        return response.blob();
    }

    // Nếu server trả về JSON
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        return response.json();
    }

    // Mặc định trả về Text
    return response.text();
}

// Export giữ nguyên
export const api = {
    get: (endpoint) => apiClient(endpoint),
    getBlob: (endpoint) => apiClient(endpoint, { method: 'GET', responseType: 'blob' }),
    post: (endpoint, body) => apiClient(endpoint, { method: 'POST', body: body }),
    put: (endpoint, body) => apiClient(endpoint, { method: 'PUT', body: body }),
    delete: (endpoint) => apiClient(endpoint, { method: 'DELETE' }),
};