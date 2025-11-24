// src/components/TechnicianSidebar.js
import { logout } from '../utils/auth.js';

export function renderTechnicianSidebar() {
    const placeholder = document.getElementById('sidebar-placeholder');
    if (!placeholder) return;

    // Lấy thông tin user từ localStorage (giống StaffSidebar)
    const userJson = localStorage.getItem('user');
    const user = userJson ? JSON.parse(userJson) : { username: 'Technician' };
    const avatarInitial = user.username ? user.username.charAt(0).toUpperCase() : 'T';

    // HTML cấu trúc giống hệt StaffSidebar
    placeholder.innerHTML = `
        <nav class="sidebar">
            <div class="sidebar-logo">
                <i class="fa-solid fa-bolt logo-icon" style="color: #3B82F6;"></i>
                <h2 style="color: white; margin-bottom: 0;">OEM EV</h2>
            </div>

            <ul class="sidebar-nav">
                <li>
                    <a href="#" id="nav-my-jobs" class="active">
                        <i class="fa-solid fa-wrench"></i>
                        Công việc của tôi
                    </a>
                </li>
                <li>
                    <a href="#" id="nav-history">
                        <i class="fa-solid fa-clock-rotate-left"></i>
                        Lịch sử sửa chữa
                    </a>
                </li>
                <li>
                    <a href="#" id="nav-profile">
                        <i class="fa-solid fa-user"></i>
                        Hồ sơ cá nhân
                    </a>
                </li>
            </ul>

            <div class="sidebar-user">
                <div class="avatar-sm" style="background-color: #3B82F6; color: white;">
                    ${avatarInitial}
                </div>
                <div style="flex-grow: 1;">
                    <span class="user-name" style="display: block; font-size: 0.9rem;">${user.username}</span>
                    <span style="font-size: 0.75rem; color: #9CA3AF;">Kỹ thuật viên</span>
                </div>
                <button id="sidebar-logout-btn" style="background: none; border: none; color: #EF4444; cursor: pointer;" title="Đăng xuất">
                    <i class="fa-solid fa-right-from-bracket"></i>
                </button>
            </div>
        </nav>
    `;
        const menuLinks = placeholder.querySelectorAll('.sidebar-nav a');
        menuLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault(); // Ngăn thẻ a load lại trang

                // Bước 1: Xóa class 'active' khỏi TẤT CẢ các link
                menuLinks.forEach(item => item.classList.remove('active'));

                // Bước 2: Thêm class 'active' cho link VỪA BẤM
                link.classList.add('active');

                // Bước 3: (Tùy chọn) Gọi hàm chuyển đổi nội dung
                // Ví dụ: Bạn có thể bắt sự kiện này ở file technician.js để ẩn hiện bảng
                const section = link.getAttribute('data-section');
                triggerSectionChange(section);
            });
        });
    function triggerSectionChange(sectionName) {
        const event = new CustomEvent('switchSection', { detail: sectionName });
        document.dispatchEvent(event);
    }
    // Gắn sự kiện Logout (Giống StaffSidebar)
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