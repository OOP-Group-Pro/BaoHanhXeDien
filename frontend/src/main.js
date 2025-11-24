// src/main.js
import './styles/login.css';
import { login, logout } from './services/authService.js'; // Import thêm logout
import { decodeToken } from './utils/auth.js';

/**
 * Hàm điều hướng thông minh dựa trên Role
 */
function redirectToDashboard(roles) {
    if (roles.includes('ROLE_ADMIN')) {
        window.location.href = '/pages/admin/index.html';
    }
   // else if (roles.includes('ROLE_MANAGER')) {
           // window.location.href = '/pages/manager/create-request.html';
        //}
    else if (roles.includes('ROLE_SC_STAFF')) {
        window.location.href = '/pages/scStaff/index.html';
    } else if (roles.includes('ROLE_EVM_STAFF')) {
        // Đảm bảo bạn đã tạo thư mục pages/evmStaff
        window.location.href = '/pages/evmStaff/index.html';
    } else if (roles.includes('ROLE_SC_TECHNICIAN') || roles.includes('ROLE_TECHNICIAN')) {
        // Đảm bảo bạn đã tạo thư mục pages/technician
        window.location.href = '/pages/technician/index.html';
    } else {
        alert('Tài khoản của bạn chưa được phân quyền truy cập dashboard nào.');
        logout(); // Đăng xuất nếu không có quyền hợp lệ
    }
}

/**
 * HÀM CHÍNH (TỰ CHẠY KHI TẢI TRANG)
 */
function main() {
    // 1. KIỂM TRA TỰ ĐỘNG CHUYỂN HƯỚNG
    const userInfo = decodeToken();

    if (userInfo) {
        // Nếu đã login, gọi hàm điều hướng và DỪNG LẠI
        redirectToDashboard(userInfo.roles);
        return; // ⬅️ Quan trọng: Dừng code tại đây để tránh chạy tiếp
    }

    // 2. LOGIC XỬ LÝ FORM
    const loginForm = document.getElementById('login-form');
    const loginButton = document.getElementById('login-button');
    const errorMessage = document.getElementById('login-error');

    if (!loginForm) return;

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;

        loginButton.disabled = true;
        loginButton.textContent = 'Đang đăng nhập...';
        errorMessage.textContent = '';

        try {
            const user = await login(username, password);

            alert('Đăng nhập thành công!');

            // Gọi hàm điều hướng sau khi login thành công
            redirectToDashboard(user.roles);

        } catch (error) {
            errorMessage.textContent = 'Lỗi: ' + error.message;
            loginButton.disabled = false;
            loginButton.textContent = 'Đăng nhập';
        }
    });
}

// Chạy hàm main
main();