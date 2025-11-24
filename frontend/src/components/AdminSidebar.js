// src/components/AdminSidebar.js
import { logout } from '../services/authService.js';
import { getUser } from '../utils/storage.js'; // Lấy thông tin user đã đăng nhập

export function renderAdminSidebar() {
    const placeholder = document.querySelector('.sidebar');
    if (!placeholder) return;

    const path = window.location.pathname;
    const user = getUser(); // { username: 'Admin Thùy', roles: [...] }

    placeholder.innerHTML = `
    <div class="sidebar-header">
        <div class="avatar">${user ? user.username.charAt(0).toUpperCase() : 'A'}</div>
        <div class="user-info">
            <strong>Admin</strong>
            <span>${user ? user.username : 'Admin User'}</span>
        </div>
    </div>
    <ul class="sidebar-nav">
        <li><a href="/pages/admin/dashboard.html" class="${path.endsWith('dashboard.html') ? 'active' : ''}"><i class="fa-solid fa-chart-line"></i>Tổng quan</a></li>
        <li><a href="/pages/admin/index.html" class="${path.endsWith('index.html') ? 'active' : ''}"><i class="fa-solid fa-users"></i>Quản lí người dùng</a></li>
        <li><a href="/pages/admin/inventory.html" class="${path.endsWith('inventory.html') ? 'active' : ''}"><i class="fa-solid fa-warehouse"></i>Quản lý Kho</a></li>
        <li><a href="/pages/admin/request.html" class="${path.endsWith('request.html') ? 'active' : ''}"><i class="fa-solid fa-cogs"></i>Phiếu đề xuất</a></li>
        <li><a href="#" id="logout-btn-sidebar"><i class="fa-solid fa-right-from-bracket"></i>Logout</a></li>
    </ul>
    `;

    // Gán sự kiện logout
    const logoutBtn = document.getElementById('logout-btn-sidebar');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (confirm('Bạn có chắc muốn đăng xuất?')) {
                logout();
            }
        });
    }
}
