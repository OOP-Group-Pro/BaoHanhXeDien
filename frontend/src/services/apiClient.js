import { getToken} from "../utils/storage.js";
import { logout } from "../utils/auth.js";

// Chi dinh base URL 1 lan duy nhat:
const API_BASE_URL = '/api/v1';  // Vite se proxy cai nay

/**
 * Ham fetch tuy chinh, tu dong them Token va xu ly loi 401
 */

async function apiClient (endpoint, options = {}) {
    const token = getToken();

    const defaultHeaders = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    // Tu dong them token vao header:
    if (token) {
        defaultHeaders['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers: defaultHeaders,
    };

    // Goi API:
    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

    // Xu li loi:
    if (!response.ok) {
        if (response.status === 401) {
            alert('Phiên đăng nhập hết hạn hoặc không hợp lệ');
            logout(); // Tu dong da ra trang login
        }

        const errorData = await response.json();
        throw new Error(errorData.message || 'Lỗi API');
    }

    // Neu method la DELETE hoac 204 No Content, khong can parse JSON
    if (response.status === 204) {
        return null;
    }

    return response.json();
}


// Tao cac ham tien ich cho team (GET, POST, PUT, DELETE)
export const api = {
    get: (endpoint) => apiClient(endpoint),
    post: (endpoint, body) => apiClient(endpoint, { method: 'POST', body: JSON.stringify(body) }),
    put: (endpoint, body) => apiClient(endpoint, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (endpoint) => apiClient(endpoint, {method: 'DELETE'}),
};