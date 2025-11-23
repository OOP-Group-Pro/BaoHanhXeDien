// src/components/TechnicianSidebar.js
import { getUser } from '../utils/storage.js';

export function renderTechnicianSidebar(placeholderId = 'sidebar-placeholder') {
    const user = getUser();
    const username = user ? user.username : 'Technician';
    const avatarInitial = username.charAt(0).toUpperCase();

    const sidebarHTML = `
        <aside class="sidebar">
            <div class="sidebar-header">
                <div class="avatar">${avatarInitial}</div>
                <div class="user-info">
                    <strong>${username}</strong>
                    <span>Kỹ thuật viên</span>
                </div>
            </div>

            <nav class="sidebar-nav">
                <a href="#" class="nav-item active" id="nav-my-jobs">
                    <i class="fa-solid fa-wrench"></i>
                    <span>Công việc của tôi</span>
                </a>

                <a href="#" class="nav-item" id="nav-history">
                    <i class="fa-solid fa-clock-rotate-left"></i>
                    <span>Lịch sử sửa chữa</span>
                </a>

                <a href="#" class="nav-item" id="nav-profile">
                    <i class="fa-solid fa-user"></i>
                    <span>Hồ sơ cá nhân</span>
                </a>
            </nav>
        </aside>
    `;

    const placeholder = document.getElementById(placeholderId);
    if (placeholder) {
        placeholder.innerHTML = sidebarHTML;
    }
}