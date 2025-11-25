// src/utils/auth.js
import { getToken, clearToken, clearUser, saveUser, getUser } from "./storage.js";
import { jwtDecode } from 'jwt-decode';

/**
 * Hàm giải mã token (Sửa lỗi: Sẽ không tự động gọi logout)
 * @returns {object|null} Thông tin user (id, roles, centerId) hoặc null nếu token hỏng/hết hạn
 */
export function decodeToken() {
    const token = getToken();
    if (!token) return null;

    try {
        const decoded = jwtDecode(token);

        // Kiểm tra xem token hết hạn chưa
        if (decoded.exp * 1000 < Date.now()) {
            // Hết hạn -> Dọn dẹp storage và trả về null (KHÔNG GỌI LOGOUT)
            console.warn("Token đã hết hạn, đang dọn dẹp storage.");
            clearToken();
            clearUser();
            return null;
        }

        // Token hợp lệ
        return decoded;

    } catch (error) {
        // Token hỏng (không giải mã được) -> Dọn dẹp storage và trả về null (KHÔNG GỌI LOGOUT)
        console.error("Token hỏng, đang dọn dẹp storage:", error);
        clearToken();
        clearUser();
        return null;
    }
}

/**
 * HÀM GÁC CỔNG CHÍNH (Hỗ trợ đa quyền)
 * @param {string|string[]} requiredRoles - (Tùy chọn) Role đơn hoặc mảng Role, ví dụ: ['ROLE_ADMIN', 'ROLE_MANAGER']
 */
export function checkAuth(requiredRoles = null) {
    const userInfo = decodeToken();

    if (!userInfo) {
        alert('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.');
        window.location.href = '/index.html';
        return null;
    }

    // Nếu có yêu cầu Role
    if (requiredRoles) {
        // Chuyển về mảng nếu là chuỗi đơn
        const allowedRoles = Array.isArray(requiredRoles) ? requiredRoles : [requiredRoles];

        // Kiểm tra: User có ít nhất 1 role nằm trong danh sách cho phép không?
        // userInfo.roles thường là mảng chuỗi ['ROLE_MANAGER', 'ROLE_USER']
        const hasPermission = userInfo.roles.some(role => allowedRoles.includes(role));

        if (!hasPermission) {
            alert('Bạn không có quyền truy cập trang này!');
            window.location.href = '/index.html';
            return null;
        }
    }

    // ... (phần lưu localStorage giữ nguyên) ...
    if (!getUser()) {
        saveUser({
            id: userInfo.sub,
            roles: userInfo.roles,
            centerId: userInfo.centerId,
            username: userInfo.sub
        });
    }

    return userInfo;
}

/**
 * Hàm Đăng xuất (Chỉ được gọi bởi nút bấm hoặc authService)
 */
export function logout() {
    clearToken();
    clearUser();
    window.location.href = '/index.html';
}