// ==============================
// LẤY USER ĐĂNG NHẬP
// ==============================
import {getMyProfile} from "../services/userService.js";

const currentUser = JSON.parse(localStorage.getItem("user"));

if (!currentUser || !currentUser.id) {
    alert("Bạn chưa đăng nhập!");
    window.location.href = "login.html";
}

// ==============================
// API BACKEND
// ==============================
import { renderAdminSidebar } from '../components/AdminSidebar.js';
const API_URL = '/api/v1/users';

renderAdminSidebar();

// ==============================
// ELEMENTS
// ==============================
const nameEl = document.getElementById("profile-name");
const roleEl = document.getElementById("profile-role");
const statusBadge = document.getElementById("profile-status-badge");
const avatarEl = document.getElementById("profile-avatar-text");

const idInput = document.getElementById("profile-id");
const fullnameInput = document.getElementById("profile-fullname");
const emailInput = document.getElementById("profile-email");
const phoneInput = document.getElementById("profile-phone");
const roleDetail = document.getElementById("profile-role-detail");
const branchInput = document.getElementById("profile-branch");

const modalFullname = document.getElementById("modal-fullname");
const modalEmail = document.getElementById("modal-email");
const modalPhone = document.getElementById("modal-phone");
const modalRole = document.getElementById("modal-role");
const modalBranch = document.getElementById("modal-branch");

// ==============================
// LOAD PROFILE FROM API
// ==============================
async function loadProfile() {
    try {
        const user = await getMyProfile();

        nameEl.textContent = user.fullname;
        roleEl.textContent = user.roleName;
        statusBadge.textContent = user.status;
        avatarEl.textContent = user.fullname.charAt(0).toUpperCase();

        idInput.value = user.id;
        fullnameInput.value = user.fullname;
        emailInput.value = user.email;
        phoneInput.value = user.phone;
        roleDetail.value = user.roleName;
        branchInput.value = user.centerName;

        modalFullname.value = user.fullname;
        modalEmail.value = user.email;
        modalPhone.value = user.phone;
        modalRole.value = user.roleName;
        modalBranch.value = user.centerName;

    } catch (err) {
        console.error("Lỗi tải profile:", err);
        // Tự động logout nếu token lỗi
        if (err.message.includes("401") || err.message.includes("403")) {
            window.location.href = "/login.html";
        }
    }
}

// ==============================
// MỞ / ĐÓNG MODAL & LOGOUT
// ==============================
document.addEventListener("DOMContentLoaded", () => {
    const editBtn = document.getElementById("editProfileBtn");
    if (editBtn) editBtn.onclick = () => document.getElementById("profileModal")?.classList.add("show");

    const closeBtn = document.getElementById("closeModal");
    if (closeBtn) closeBtn.onclick = () => document.getElementById("profileModal")?.classList.remove("show");

    const saveBtn = document.getElementById("saveProfileBtn");
    if (saveBtn) saveBtn.onclick = async () => {
        const payload = {
            email: modalEmail.value,
            phone: modalPhone.value,
            // Không gửi role, branch lên vì user thường không được sửa cái này
        };

        try {
            // KHÔNG CẦN TRUYỀN ID NỮA
            await updateMyProfile(payload);

            alert("Cập nhật thành công!");
            profileModal.classList.remove("show");
            loadProfile();

        } catch (err) {
            console.error(err);
            alert("Lỗi khi cập nhật!");
        }
    };

    const logoutBtn = document.getElementById("btn-logout");
    if (logoutBtn) logoutBtn.onclick = () => {
        localStorage.removeItem("user");
        window.location.href = "login.html";
    };
});

// ==============================
// RUN
// ==============================
loadProfile();
