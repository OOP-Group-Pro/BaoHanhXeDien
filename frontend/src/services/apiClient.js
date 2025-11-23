import { getToken } from '../utils/storage.js';
import { logout } from '../utils/auth.js';

const API_BASE_URL = '/api/v1';

// Hàm loại bỏ dấu "/" bị dư ở cuối endpoint
const clean = (path) => path.replace(/\/+$/, "");

async function apiClient(endpoint, options = {}) {
    const token = getToken();

    const headers = { ...options.headers };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    // Set Content-Type cho POST & PUT
    if (options.method && ['POST', 'PUT'].includes(options.method)) {
        if (options.body && !(options.body instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
        }
    }

    const config = {
        ...options,
        headers
    };

    // GET/DELETE không được có body
    if (config.method === 'GET' || config.method === 'DELETE') {
        delete config.body;
    }

    const url = `${API_BASE_URL}${clean(endpoint)}`;

    const response = await fetch(url, config);

    // Blob
    if (options.responseType === 'blob') {
        if (!response.ok) throw new Error('Lỗi tải file');
        return await response.blob();
    }

    const text = await response.text();

    let data;
    try {
        data = JSON.parse(text);
    } catch {
        data = text;
    }

    if (!response.ok) {
        if (response.status === 401) {
            logout();
            throw new Error('Phiên đăng nhập hết hạn');
        }

        const message =
            typeof data === 'object' && data?.message
                ? data.message
                : typeof data === 'string'
                ? data
                : 'Lỗi API';

        console.error("API Error:", message, "Path:", clean(endpoint));
        throw new Error(message);
    }

    return data;
}

export const api = {
    get: (endpoint) => apiClient(clean(endpoint), { method: 'GET' }),
    getBlob: (endpoint) =>
        apiClient(clean(endpoint), { method: 'GET', responseType: 'blob' }),
    post: (endpoint, body) =>
        apiClient(clean(endpoint), {
            method: 'POST',
            body: body instanceof FormData ? body : JSON.stringify(body),
        }),
    put: (endpoint, body) =>
        apiClient(clean(endpoint), {
            method: 'PUT',
            body: body instanceof FormData ? body : JSON.stringify(body),
        }),
    delete: (endpoint) =>
        apiClient(clean(endpoint), { method: 'DELETE' }),
};
