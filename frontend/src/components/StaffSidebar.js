// src/components/StaffSidebar.js
import { logout } from '../utils/auth.js';

export function renderStaffSidebar() {
    const placeholder = document.getElementById('sidebar-placeholder');
    if (!placeholder) return;

    // Lấy tên user từ localStorage (nếu có) để hiển thị đẹp hơn
    const userJson = localStorage.getItem('user');
    const user = userJson ? JSON.parse(userJson) : { username: 'SC Staff' };

    // Kiểm tra đường dẫn hiện tại để active menu
    const path = window.location.pathname;
    const isActive = (p) => path.includes(p) ? 'active' : '';

    placeholder.innerHTML = `
        <nav class="sidebar">
            <div class="sidebar-logo">
                <i class="fa-solid fa-bolt logo-icon" style="color: #3B82F6;"></i>
                <h2 style="color: white; margin-bottom: 0;">OEM EV</h2>
            </div>

            <ul class="sidebar-nav">
                <li>
                    <a href="/pages/scStaff/index.html" class="${isActive('/pages/scStaff/index.html')}">
                        <i class="fa-solid fa-tachometer-alt"></i>
                        Dashboard & Claims
                    </a>
                </li>
                <li>
                    <a href="/pages/scStaff/create-claim.html" class="${isActive('/pages/scStaff/create-claim.html')}">
                        <i class="fa-solid fa-file-circle-plus"></i>
                        Tạo Claim Mới
                    </a>
                </li>
                <li>
                    <a href="/pages/scStaff/vehicle-lookup.html" class="">
                        <i class="fa-solid fa-car"></i>
                        Tra cứu Xe
                    </a>
                </li>
                <li>
                    <a href="/pages/scStaff/appointments.html" class="">
                        <i class="fa-solid fa-calendar-check"></i>
                        Lịch hẹn
                    </a>
                </li>
                </li>
                    <a href="/pages/scStaff/profile.html" class="">
                        <i class="fa-solid fa-calendar-check"></i>
                        Hồ sơ cá nhân
                    </a>
                </li>

            </ul>

            <div class="sidebar-user">
                <div class="avatar-sm" style="background-color: #3B82F6; color: white;">
                    ${user.username.charAt(0).toUpperCase()}
                </div>
                <div style="flex-grow: 1;">
                    <span class="user-name" style="display: block; font-size: 0.9rem;">${user.username}</span>
                    <span style="font-size: 0.75rem; color: #9CA3AF;">SC Staff</span>
                </div>
                <button id="sidebar-logout-btn" style="background: none; border: none; color: #EF4444; cursor: pointer;" title="Đăng xuất">
                    <i class="fa-solid fa-right-from-bracket"></i>
                </button>
            </div>
        </nav>
    `;

    // Gắn sự kiện Logout cho nút mới trong Sidebar
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