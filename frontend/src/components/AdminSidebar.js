// src/components/EvmSidebar.js
import { logout } from '../services/authService.js';
import { getUser } from '../utils/storage.js'; // Lấy thông tin user đã đăng nhập

export function renderEvmSidebar() {
    const placeholder = document.getElementById('sidebar-placeholder');
    if (!placeholder) return;

    // Lấy pathname hiện tại
    const path = window.location.pathname;
    const user = getUser(); // Lấy thông tin user (ví dụ: { username: 'Admin Thùy' })

    // HTML của Sidebar (dựa trên file template của bạn)
    placeholder.innerHTML = `
    <nav class="sidebar">
        <div class="sidebar-logo">
            <i class="fa-solid fa-bolt logo-icon"></i>
            <h2>OEM EV</h2>
        </div>

        <ul class="sidebar-nav">
            <li>
                <a href="/pages/evmStaff/dashboard.html" class="${path.endsWith('dashboard.html') ? 'active' : ''}">
                    <i class="fa-solid fa-tachometer-alt"></i>Dashboard
                </a>
            </li>
            <li>
                <a href="/pages/evmStaff/claims.html" class="${path.endsWith('claims.html') ? 'active' : ''}">
                    <i class="fa-solid fa-file-invoice"></i>Claims
                </a>
            </li>
            <li>
                <a href="/pages/admin/parts.html" class="${path.endsWith('parts.html') ? 'active' : ''}">
                    <i class="fa-solid fa-cogs"></i>Parts
                </a>
            </li>
            <li>
                <a href="/pages/admin/inventory.html" class="${path.endsWith('inventory.html') ? 'active' : ''}">
                    <i class="fa-solid fa-warehouse"></i>Inventory
                </a>
            </li>
            <li>
                <a href="/pages/admin/users.html" class="${path.endsWith('users.html') ? 'active' : ''}">
                    <i class="fa-solid fa-users"></i>Users
                </a>
            </li>
            <li>
                <a href="/pages/evmStaff/campaigns.html" class="${path.endsWith('campaigns.html') ? 'active' : ''}">
                    <i class="fa-solid fa-flag"></i>Campaign
                </a>
            </li>
            <li>
                <a href="/pages/evmStaff/reports.html" class="${path.endsWith('reports.html') ? 'active' : ''}">
                    <i class="fa-solid fa-chart-line"></i>Reports
                </a>
            </li>
        </ul>
        
        <div class="sidebar-user">
            <div class="avatar-sm">${user ? user.username.charAt(0).toUpperCase() : 'E'}</div>
            <span class="user-name">${user ? user.username : 'EVM Staff'}</span>
            <button id="logout-btn-sidebar" title="Đăng xuất">
                <i class="fa-solid fa-right-from-bracket"></i>
            </button>
        </div>
    </nav>
    `;

    // Gán sự kiện cho nút Logout (sau khi đã render)
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