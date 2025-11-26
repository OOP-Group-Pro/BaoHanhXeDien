import { getMyProfile, updateMyProfile } from "../services/userService.js";
import { renderSidebarByRole } from '../utils/SidebarManager.js'; // Sử dụng bộ quản lý sidebar
import { getUser } from '../utils/storage.js';

// 1. Kiểm tra đăng nhập
const currentUser = getUser();
if (!currentUser) {
    window.location.href = "/login.html";
}

// 2. Render Sidebar ĐÚNG THEO ROLE
renderSidebarByRole();

// 3. Elements
const els = {
    name: document.getElementById("profile-name"),
    role: document.getElementById("profile-role"),
    status: document.getElementById("profile-status-badge"),
    avatar: document.getElementById("profile-avatar-text"),
    id: document.getElementById("profile-id"),
    fullname: document.getElementById("profile-fullname"),
    email: document.getElementById("profile-email"),
    phone: document.getElementById("profile-phone"),
    center: document.getElementById("profile-center"),
    lastLogin: document.getElementById("profile-last-login"),

    // Modal
    modal: document.getElementById("profileModal"),
    modalEmail: document.getElementById("modal-email"),
    modalPhone: document.getElementById("modal-phone"),
    editBtn: document.getElementById("editProfileBtn"),
    closeBtn: document.getElementById("closeModal"),
    saveBtn: document.getElementById("saveProfileBtn")
};

// 4. Load Data
async function loadProfile() {
    try {
        // Gọi API /users/me (Backend tự biết ai đang gọi nhờ Token)
        const user = await getMyProfile();

        const displayName = user.fullName || user.username;
        const roleDisplay = (user.roles || []).map(r => r.roleName || r).join(', ');
        const centerDisplay = user.serviceCenterId ? `Trạm ${user.serviceCenterId}` : "Hội sở (HQ)";

        // Format Last Login
        let loginTime = "Chưa ghi nhận";
        if (user.lastLogin) {
            loginTime = new Date(user.lastLogin).toLocaleString('vi-VN');
        }

        // Fill Data
        els.name.textContent = displayName;
        els.role.textContent = roleDisplay;
        els.status.textContent = user.status || 'ACTIVE';
        els.avatar.textContent = displayName.charAt(0).toUpperCase();

        els.id.value = user.userId;
        els.fullname.value = displayName;
        els.email.value = user.email || "";
        els.phone.value = user.phone || "";
        els.center.value = centerDisplay;
        els.lastLogin.value = loginTime;

        // Fill Modal
        els.modalEmail.value = user.email || "";
        els.modalPhone.value = user.phone || "";

    } catch (err) {
        console.error(err);
        if (err.message.includes("401")) window.location.href = "/login.html";
    }
}

// 5. Events
document.addEventListener("DOMContentLoaded", () => {
    els.editBtn.onclick = () => els.modal.classList.add("show");
    els.closeBtn.onclick = () => els.modal.classList.remove("show");

    els.saveBtn.onclick = async () => {
        const payload = {
            email: els.modalEmail.value.trim(),
            phone: els.modalPhone.value.trim()
        };

        try {
            els.saveBtn.textContent = "Đang lưu...";
            els.saveBtn.disabled = true;

            await updateMyProfile(payload);

            alert("Cập nhật thành công!");
            els.modal.classList.remove("show");
            loadProfile();

        } catch (err) {
            alert("Lỗi: " + err.message);
        } finally {
            els.saveBtn.textContent = "Lưu thay đổi";
            els.saveBtn.disabled = false;
        }
    };
});

// Run
loadProfile();