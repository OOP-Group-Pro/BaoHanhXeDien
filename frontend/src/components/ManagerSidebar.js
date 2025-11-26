// src/components/AdminSidebar.js
import { logout } from '../services/authService.js';
import { getUser } from '../utils/storage.js'; // Lấy thông tin user đã đăng nhập

export function renderManagerSidebar() {
    const placeholder = document.querySelector('.sidebar');
    if (!placeholder) return;

    const path = window.location.pathname;
    const user = getUser(); // { username: 'Admin Thùy', roles: [...] }

    placeholder.innerHTML = `
    <div class="sidebar-header">
        <div class="avatar">${user ? user.username.charAt(0).toUpperCase() : 'A'}</div>
        <div class="user-info">
            <strong>Manager</strong>
            <span>${user ? user.username : 'Manager User'}</span>
        </div>
    </div>
    <ul class="sidebar-nav">
        <li><a href="/pages/manager/create-request.html" class="${path.endsWith('dashboard.html') ? 'active' : ''}"><i class="fa-solid fa-chart-line"></i>Danh sách phiếu </a></li>
        <li><a href="../common/profile.html" class="${path.endsWith('profile.html') ? 'active' : ''}"><i class="fa-solid fa-user"></i>Hồ sơ cá nhân</a></li>
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
