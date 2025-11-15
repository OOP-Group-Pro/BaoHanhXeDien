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
 * HÀM GÁC CỔNG CHÍNH
 * (Dùng ở đầu mỗi file JS của các trang được bảo vệ, VÍ DỤ: scStaff.js)
 * @param {string} requiredRole - (Tùy chọn) Tên vai trò yêu cầu, ví dụ: "ROLE_ADMIN"
 */
export function checkAuth (requiredRole = null) {
    const userInfo = decodeToken(); // (Đã bao gồm kiểm tra token, hết hạn)

    if (!userInfo) {
        // 1. Nếu không có thông tin (chưa login hoặc token hỏng/hết hạn)
        alert('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.');
        window.location.href = '/index.html'; // Chuyển về trang login
        return null; // ⬅️ Trả về null để file JS kia dừng lại
    }

    if (requiredRole && !userInfo.roles.includes(requiredRole)) {
        // 2. Nếu có yêu cầu vai trò, nhưng user không có vai trò đó
        alert('Bạn không có quyền truy cập trang này!');
        window.location.href = '/index.html'; // ⬅️ Chuyển về trang login (an toàn)
        return null; // ⬅️ Trả về null
    }

    // 3. Hợp lệ!
    // Đồng bộ thông tin user trong localStorage (nếu chưa có)
    if (!getUser()) {
        saveUser({
            id: userInfo.sub,
            roles: userInfo.roles,
            centerId: userInfo.centerId,
            username: userInfo.sub // (JwtService của bạn không lưu username, tạm dùng ID)
        });
    }

    return userInfo; // Trả về info user để trang có thể dùng
}

/**
 * Hàm Đăng xuất (Chỉ được gọi bởi nút bấm hoặc authService)
 */
export function logout() {
    clearToken();
    clearUser();
    window.location.href = '/index.html';
}