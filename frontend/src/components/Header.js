// src/components/Header.js
import { logout } from '../utils/auth.js';
import { getUser } from '../utils/storage.js'; // Giả sử bạn đã có hàm này

export function renderHeader() {
    const placeholder = document.getElementById('header-placeholder');
    if (!placeholder) return;

    // Lấy thông tin user từ localStorage (nếu có)
    const user = getUser(); // Hàm này bạn tự viết trong storage.js nhé
    const username = user ? user.username : 'Guest';

    placeholder.innerHTML = `
        <header class="main-header">
            <div class="logo">OEM EV Warranty</div>
            <div class="user-menu">
                <span>Chào, ${username}</span>
                <button id="logout-btn" class="button-logout">Đăng xuất</button>
            </div>
        </header>
    `;

    // Gắn sự kiện cho nút logout
    document.getElementById('logout-btn').addEventListener('click', () => {
        logout(); // Gọi hàm logout từ auth.js
    });
}