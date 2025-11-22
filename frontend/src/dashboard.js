// Logout
document.getElementById("logout-btn").addEventListener("click", async () => {
    try {
        // 1. Gọi API logout backend nếu dùng session cookie
        await fetch("/api/logout", { method: "POST", credentials: "include" });

        // 2. Xóa token JWT nếu có
        localStorage.removeItem("jwtToken");

        // 3. Chuyển hẳn sang login
        window.location.replace("/login.html"); // <-- quan trọng: replace() tránh quay lại dashboard
    } catch (err) {
        console.error(err);
    }
});


// Lấy dữ liệu Recent Requests
async function fetchRecentRequests() {
    try {
        const res = await fetch("/api/requests"); // REST API backend
        const data = await res.json();
        const tbody = document.querySelector("#requests-table tbody");
        tbody.innerHTML = data.length
            ? data.map(r => `<tr><td>${r.id}</td><td>${r.status}</td></tr>`).join("")
            : '<tr><td colspan="2">Không có dữ liệu</td></tr>';
    } catch (err) {
        console.error(err);
    }
}

// Lấy dữ liệu tóm tắt User
async function fetchUserSummary() {
    try {
        const res = await fetch("/api/users/summary");
        const data = await res.json();
        const tbody = document.querySelector("#users-summary-table tbody");
        tbody.innerHTML = data.length
            ? data.map(u => `<tr><td>${u.role}</td><td>${u.count}</td></tr>`).join("")
            : '<tr><td colspan="2">Không có dữ liệu</td></tr>';
    } catch (err) {
        console.error(err);
    }
}

// Load khi mở dashboard
fetchRecentRequests();
fetchUserSummary();
