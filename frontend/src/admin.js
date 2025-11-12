// src/scAdmin.js

import { checkAuth } from './utils/auth.js';
import { renderHeader } from './components/Header.js';
import { renderAdminSidebar } from './components/AdminSidebar.js';
import { getAllUsers, createUser, updateUser, deleteUser } from './services/userService.js';
import { logout } from './services/authService.js'; // (bạn cần tạo file authService.js)

// --- CHẠY CHUNG CHO MỌI TRANG ADMIN ---

// 1. GÁC CỔNG: Kiểm tra xem có phải ADMIN không
const userInfo = checkAuth('ROLE_ADMIN');
if (!userInfo) return; // Dừng thực thi nếu không phải

// 2. VẼ GIAO DIỆN CHUNG
renderHeader();
renderAdminSidebar();

// --- LOGIC CHO TỪNG TRANG CỤ THỂ ---

// Dashboard tổng hợp
if (window.location.pathname.endsWith('/admin/index.html')) {
    loadAdminDashboard();
}

// Trang quản lý user
if (window.location.pathname.endsWith('/admin/users.html')) {
    loadUserList();
    setupCreateUserForm();
}

// --- Các hàm thực thi ---

/**
 * Load dữ liệu Dashboard admin (ví dụ tổng hợp số lượng user, claim,...)
 */
async function loadAdminDashboard() {
    // TODO: gọi API tổng hợp nếu có
    console.log('Dashboard admin load...');
}

/**
 * Load danh sách user
 */
async function loadUserList() {
    try {
        const users = await getAllUsers();
        const tbody = document.getElementById('user-table-body');
        tbody.innerHTML = '';

        if (!users.length) {
            tbody.innerHTML = '<tr><td colspan="5">Không có user nào</td></tr>';
            return;
        }

        users.forEach(u => {
            tbody.innerHTML += `
                <tr>
                    <td>${u.userId}</td>
                    <td>${u.username}</td>
                    <td>${u.email}</td>
                    <td>${u.phone}</td>
                    <td>${u.status}</td>
                </tr>
            `;
        });
    } catch (err) {
        alert('Lỗi tải danh sách user: ' + err.message);
    }
}

/**
 * Gắn sự kiện cho Form tạo user mới
 */
function setupCreateUserForm() {
    const form = document.getElementById('user-form');
    if (!form) return;

    form.addEventListener('submit', async e => {
        e.preventDefault();
        const button = form.querySelector('button[type="submit"]');
        button.disabled = true;
        button.textContent = 'Đang gửi...';

        const username = form.querySelector('#username').value;
        const email = form.querySelector('#email').value;
        const phone = form.querySelector('#phone').value;
        const password = form.querySelector('#password').value;
        const roleName = form.querySelector('#role').value;

        try {
            await createUser({ username, email, phone, password }, roleName);
            alert('Tạo user thành công!');
            loadUserList(); // reload bảng
        } catch (err) {
            alert('Lỗi tạo user: ' + err.message);
        } finally {
            button.disabled = false;
            button.textContent = 'Tạo';
        }
    });
}
