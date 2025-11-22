// ===============================
//  CREATE-REQUEST.JS CHO MANAGER
// ===============================

// Backend URL
const BASE_URL = "http://localhost:8080";

// Lấy token từ localStorage (nếu đang dùng Bearer token)
function getToken() {
    return localStorage.getItem("token");
}

// ========================
// Load danh sách phiếu của Manager
// ========================
async function loadMyRequests() {
    const tbody = document.getElementById("request-table-body");
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;">Đang tải...</td></tr>`;

    try {
        const res = await fetch(`${BASE_URL}/api/v1/staff-requests/my`, {
            method: "GET",
            headers: {
                "Authorization": "Bearer " + getToken()
            }
        });

        if (!res.ok) throw new Error("Không load được dữ liệu");

        const data = await res.json();
        if (data.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;">Chưa có phiếu nào</td></tr>`;
            return;
        }

        tbody.innerHTML = "";
        data.forEach(r => {
            tbody.innerHTML += `
            <tr>
                <td>${r.requestId}</td>
                <td>${r.fullName}</td>
                <td>${r.username}</td>
                <td>${r.email || ""}</td>
                <td>${r.phone || ""}</td>
                <td>${r.proposedRoles?.map(role => role.roleName).join(", ") || ""}</td>
                <td>${r.status}</td>
                <td>-</td>
            </tr>`;
        });

    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;">${err.message}</td></tr>`;
    }
}

// ========================
// Mở modal tạo phiếu
// ========================
function openCreateModal() {
    const modal = document.getElementById("request-modal");
    modal.style.display = "flex";

    document.getElementById("modal-title").innerText = "Tạo Phiếu Mới";
    document.getElementById("request-id").value = "";
    document.getElementById("fullName").value = "";
    document.getElementById("username").value = "";
    document.getElementById("email").value = "";
    document.getElementById("phone").value = "";
    document.getElementById("role").value = "ROLE_USER";

    // Nếu có serviceCenterId input
    const scInput = document.getElementById("serviceCenterId");
    if(scInput) scInput.value = "";
}

// ========================
// Đóng modal
// ========================
function closeCreateModal() {
    document.getElementById("request-modal").style.display = "none";
}

// ========================
// Gửi phiếu
// ========================
async function saveRequest() {

    const fullName = document.getElementById("fullName").value.trim();
    const username = document.getElementById("username").value.trim();
    const email = document.getElementById("email").value.trim();
    const phone = document.getElementById("phone").value.trim();
    const role = document.getElementById("role").value;

    // Nếu có serviceCenterId input
    const serviceCenterInput = document.getElementById("serviceCenterId");
    const serviceCenterId = serviceCenterInput ? parseInt(serviceCenterInput.value) : null;

    if (!fullName || !username) {
        Swal.fire("Thiếu thông tin", "Full Name và Username bắt buộc!", "warning");
        return;
    }

    const requestBody = { fullName, username, email, phone, serviceCenterId, proposedRoles: [{ roleName: role }] };

    try {
        const res = await fetch(`${BASE_URL}/api/v1/staff-requests/create`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + getToken()
            },
            body: JSON.stringify(requestBody)
        });

        if (!res.ok) throw new Error("Lỗi gửi phiếu");

        Swal.fire("Thành công!", "Phiếu đã được gửi!", "success");

        closeCreateModal();
        loadMyRequests();

    } catch (err) {
        Swal.fire("Lỗi!", err.message, "error");
    }
}

// ========================
// EVENT LISTENER
// ========================
document.addEventListener("DOMContentLoaded", () => {

    // Load danh sách phiếu Manager
    loadMyRequests();

    // Nút mở modal
    document.getElementById("create-request-btn")?.addEventListener("click", openCreateModal);

    // Nút đóng modal
    document.getElementById("close-request-modal")?.addEventListener("click", closeCreateModal);

    // Click ngoài modal để đóng
    document.getElementById("request-modal")?.addEventListener("click", (e) => {
        if (e.target.id === "request-modal") closeCreateModal();
    });

    // Nút gửi phiếu
    document.getElementById("save-request-btn")?.addEventListener("click", saveRequest);
});
// Nếu dùng token/localStorage
document.getElementById("logout-btn").addEventListener("click", () => {
    // Xóa token JWT khỏi localStorage/sessionStorage
    localStorage.removeItem("jwtToken");
    // Chuyển về trang login
    window.location.href = "../../login.html";
});
