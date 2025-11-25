// src/components/AdminSidebar.js
import { logout } from '../services/authService.js';
import { getUser } from '../utils/storage.js';

export function renderAdminSidebar() {
    const placeholder = document.querySelector('.sidebar');
    if (!placeholder) return;

    const path = window.location.pathname;
    const user = getUser();

    // Logic active thông minh hơn (check includes thay vì endsWith để bao quát)
    const isActive = (p) => path.includes(p) ? 'active' : '';

    placeholder.innerHTML = `
    <div class="sidebar-header">
        <div class="avatar">${user ? user.username.charAt(0).toUpperCase() : 'A'}</div>
        <div class="user-info">
            <strong>Admin</strong>
            <span>${user ? user.username : 'Administrator'}</span>
        </div>
    </div>
    <ul class="sidebar-nav">
        <li>
            <a href="/pages/admin/dashboard.html" class="${isActive('dashboard.html')}">
                <i class="fa-solid fa-chart-line"></i> Tổng quan
            </a>
        </li>
        <li>
            <a href="/pages/admin/index.html" class="${isActive('index.html')}">
                <i class="fa-solid fa-users"></i> Quản lý Người dùng
            </a>
        </li>
        <li>
            <a href="/pages/admin/policies.html" class="${isActive('policies.html')}">
                <i class="fa-solid fa-shield-halved"></i> Chính sách BH
            </a>
        </li>
        <li>
            <a href="/pages/admin/inventory.html" class="${isActive('inventory.html')}">
                <i class="fa-solid fa-box-open"></i> Quản lý Kho
            </a>
        </li>
        <li>
            <a href="/pages/admin/request.html" class="${isActive('request.html')}">
                <i class="fa-solid fa-file-signature"></i> Phiếu Đề xuất
            </a>
        </li>
        <li>
            <a href="#" id="logout-btn-sidebar">
                <i class="fa-solid fa-right-from-bracket"></i> Đăng xuất
            </a>
        </li>
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