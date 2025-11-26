// src/components/EvmSidebar.js
import { logout } from '../utils/auth.js';

export function renderEvmSidebar() {
    const placeholder = document.getElementById('sidebar-placeholder');
    if (!placeholder) return;

    // Lấy tên user từ localStorage
    const userJson = localStorage.getItem('user');
    const user = userJson ? JSON.parse(userJson) : { username: 'EVM Staff' };

    // Kiểm tra đường dẫn hiện tại để active menu
    const path = window.location.pathname;
    const isActive = (p) => path.includes(p) ? 'active' : '';

    placeholder.innerHTML = `
        <nav class="sidebar">
            <div class="sidebar-logo">
                <i class="fa-solid fa-bolt logo-icon" style="color: #10B981;"></i> <h2 style="color: white; margin-bottom: 0;">OEM EV</h2>
            </div>

            <ul class="sidebar-nav">
                <li>
                    <a href="/pages/evmStaff/index.html" class="${isActive('/pages/evmStaff/index.html')}">
                        <i class="fa-solid fa-chart-line"></i>
                        Dashboard
                    </a>
                </li>
                <li>
                    <a href="/pages/evmStaff/campaigns.html" class="${isActive('/pages/evmStaff/campaigns.html')}">
                        <i class="fa-solid fa-bullhorn"></i>
                        Quản lý Chiến dịch
                    </a>
                </li>

                <li>
                    <a href="/pages/evmStaff/reports.html" class="">
                        <i class="fa-solid fa-chart-pie"></i>
                        Báo cáo
                    </a>
                </li>
                </li>

                 <a href="../common/profile.html" class="">
                                        <i class="fa-solid fa-chart-pie"></i>
                                        Hồ sơ cá nhân
                                    </a>
                                </li>
            </ul>

            <div class="sidebar-user">
                <div class="avatar-sm" style="background-color: #10B981; color: white;">
                    ${user.username.charAt(0).toUpperCase()}
                </div>
                <div style="flex-grow: 1;">
                    <span class="user-name" style="display: block; font-size: 0.9rem;">${user.username}</span>
                    <span style="font-size: 0.75rem; color: #9CA3AF;">EVM Staff (Hãng)</span>
                </div>
                <button id="sidebar-logout-btn" style="background: none; border: none; color: #EF4444; cursor: pointer;" title="Đăng xuất">
                    <i class="fa-solid fa-right-from-bracket"></i>
                </button>
            </div>
        </nav>
    `;

    // Gắn sự kiện Logout
    const logoutBtn = document.getElementById('sidebar-logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (confirm('Bạn có chắc muốn đăng xuất?')) {
                logout();
            }
        });
    }
}