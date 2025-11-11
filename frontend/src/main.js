// src/main.js
import './styles/main.css'; // Import CSS
import { login } from './services/authService.js';
import { decodeToken } from './utils/auth.js';

// Kiểm tra xem user đã login chưa? Nếu rồi, tự động chuyển hướng
const userInfo = decodeToken();
if (userInfo) {
    // Nếu là admin
    if (userInfo.roles.includes('ROLE_ADMIN')) {
        window.location.href = '/pages/admin/index.html';
    } else {
        // Mặc định cho các role khác
        window.location.href = '/pages/staff/index.html';
    }
}

// Xử lý form
const loginForm = document.getElementById('login-form');
const loginButton = document.getElementById('login-button');
const errorMessage = document.getElementById('login-error');

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault(); // Ngăn form reload

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    // Vô hiệu hóa nút bấm
    loginButton.disabled = true;
    loginButton.textContent = 'Đang đăng nhập...';
    errorMessage.textContent = '';

    try {
        const user = await login(username, password);

        // Đăng nhập thành công, chuyển hướng dựa trên vai trò
        alert('Đăng nhập thành công!');
        if (user.roles.includes('ROLE_ADMIN')) {
            window.location.href = '/pages/admin/index.html';
        } else {
            // Mặc định cho các role khác (SC_STAFF, TECHNICIAN, v.v.)
            window.location.href = '/pages/staff/index.html';
        }

    } catch (error) {
        // Đăng nhập thất bại
        errorMessage.textContent = 'Lỗi: ' + error.message;
        loginButton.disabled = false;
        loginButton.textContent = 'Đăng nhập';
    }
});