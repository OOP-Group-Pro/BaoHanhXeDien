// src/components/TechnicianSidebar.js

export function renderTechnicianSidebar(placeholderId = 'sidebar-placeholder') {
    const sidebarHTML = `
        <aside class="sidebar">
            <nav class="sidebar-nav">
                <a href="/pages/technician/index.html" class="nav-item active" id="nav-my-jobs">
                    <i class="fa-solid fa-wrench"></i>
                    <span>Công việc của tôi</span>
                </a>
                <a href="/pages/technician/history.html" class="nav-item">
                    <i class="fa-solid fa-history"></i>
                    <span>Lịch sử sửa chữa</span>
                </a>
                <a href="/pages/technician/profile.html" class="nav-item">
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