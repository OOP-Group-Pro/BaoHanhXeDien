import { getToken } from '../utils/storage.js';
import { logout } from '../utils/auth.js';

const API_BASE_URL = '/api/v1';

async function apiClient(endpoint, options = {}) {
    const token = getToken();
    const headers = { ...options.headers };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    // Xử lý FormData (nếu body là FormData thì để browser tự set Content-Type)
    if (!(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }

    const config = {
        ...options,
        headers: headers,
    };

    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

    // -------------------------------------------------------
    // ⬇️ SỬA LỖI: ĐỌC BODY 1 LẦN DUY NHẤT ĐỂ TRÁNH LỖI STREAM ⬇️
    // -------------------------------------------------------

    // 1. Kiểm tra nếu response là Blob (File) thì trả về ngay
    if (options.responseType === 'blob') {
        if (!response.ok) throw new Error('Lỗi tải file');
        return response.blob();
    }

    // 2. Đọc toàn bộ body dưới dạng Text trước
    const responseText = await response.text();

    // 3. Thử Parse JSON
    let data;
    try {
        data = JSON.parse(responseText);
    } catch (e) {
        // Nếu không phải JSON, data chính là text
        data = responseText;
    }

    // 4. Xử lý Lỗi (Dựa trên status code)
    if (!response.ok) {
        if (response.status === 401) {
            logout();
            throw new Error('Phiên đăng nhập hết hạn.');
        }

        // Lấy message lỗi ưu tiên từ JSON, nếu không thì dùng text
        const errorMessage = (typeof data === 'object' && data.message)
            ? data.message
            : (typeof data === 'string' ? data : 'Lỗi API');

        console.error('API Error:', errorMessage, 'Path:', endpoint);
        throw new Error(errorMessage);
    }

    // 5. Trả về dữ liệu thành công
    return data;
}

export const api = {
    get: (endpoint) => apiClient(endpoint),
    getBlob: (endpoint) => apiClient(endpoint, { method: 'GET', responseType: 'blob' }),
    post: (endpoint, body) => apiClient(endpoint, { method: 'POST', body: (body instanceof FormData) ? body : JSON.stringify(body) }),
    put: (endpoint, body) => apiClient(endpoint, { method: 'PUT', body: (body instanceof FormData) ? body : JSON.stringify(body) }),
    delete: (endpoint) => apiClient(endpoint, { method: 'DELETE' }),
};