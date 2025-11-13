// Import hàm lấy thông tin user đã đăng nhập
import { getUser } from '../utils/storage.js';

export function renderTechnicianSidebar() {
    const placeholder = document.getElementById('sidebar-placeholder');
    if (!placeholder) return;

    // Lấy thông tin user (từ auth.js/storage.js)
    const user = getUser();
    const username = user ? user.username : 'Technician'; // Lấy tên thật
    const avatarInitial = username.charAt(0).toUpperCase();

    // Đây là HTML sidebar Kỹ thuật viên
    placeholder.innerHTML = `
        <nav class="sidebar">
            <div class="sidebar-header">
                <div class="avatar">${avatarInitial}</div>
                <div class="user-info">
                    <strong>${username}</strong>
                    <span>Technician Role</span>
                </div>
            </div>
            <ul class="sidebar-nav">
                <li><a href="#"><i class="fa-solid fa-tachometer-alt"></i>Dashboard</a></li>

                <li><a href="/pages/technician/technician.html" class="active" id="nav-my-jobs">
                    <i class="fa-solid fa-wrench"></i>Công việc của tôi
                </a></li>

                <li><a href="#"><i class="fa-solid fa-file-invoice"></i>Claims (Tất cả)</a></li>
                <li><a href="#"><i class="fa-solid fa-cogs"></i>Parts</a></li>
            </ul>
        </nav>
    `;
}