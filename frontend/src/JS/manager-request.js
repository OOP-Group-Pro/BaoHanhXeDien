// src/JS/manager-request.js

import { logout } from "../services/authService.js";
import { checkAuth } from '../utils/auth.js';
import { api } from '../services/apiClient.js';
import { renderAdminSidebar } from "../components/AdminSidebar.js";

async function initRequestPage() {
    // 1. Kiểm tra quyền (Admin hoặc Manager)
    // Lưu ý: Trang này có vẻ đang dùng chung cho cả Admin duyệt và Manager xem
    // Nếu Manager chỉ được xem của mình -> cần logic khác (gọi API /my)
    // Tạm thời giả định đây là trang ADMIN DUYỆT.
    const userInfo = checkAuth(['ROLE_ADMIN', 'ROLE_MANAGER']);
    if (!userInfo) return;

    renderAdminSidebar();

    const logoutBtn = document.getElementById('logout-btn');
    const reloadBtn = document.getElementById('reload-data-btn');
    const searchInput = document.querySelector('.search-bar input');
    const tableBody = document.getElementById('request-table-body');

    // --- EVENTS ---
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            logout();
        });
    }

    if (reloadBtn) {
        reloadBtn.addEventListener('click', loadRequestList);
    }

    if (searchInput) {
        searchInput.addEventListener('input', () => {
            const keyword = searchInput.value.trim().toLowerCase();
            filterRequests(keyword);
        });
    }

    let allRequests = [];

    // --- FUNCTIONS ---

    // 1. Tải danh sách
    async function loadRequestList() {
        if (!tableBody) return;
        tableBody.innerHTML = '<tr><td colspan="8" class="text-center">Đang tải dữ liệu...</td></tr>';

        try {
            // ⚠️ LOGIC QUAN TRỌNG:
            // Nếu là Admin -> Gọi /all
            // Nếu là Manager -> Gọi /my
            const isManager = userInfo.roles.includes('ROLE_MANAGER') && !userInfo.roles.includes('ROLE_ADMIN');
            const endpoint = isManager ? '/staff-requests/my' : '/staff-requests/all';

            const requests = await api.get(endpoint);
            allRequests = requests;
            renderRequestTable(requests);
        } catch (err) {
            console.error('Lỗi tải dữ liệu:', err);
            tableBody.innerHTML = '<tr><td colspan="8" class="text-center text-danger">Lỗi tải dữ liệu</td></tr>';
        }
    }

    // 2. Render Bảng
    function renderRequestTable(requests) {
        if (!requests || !requests.length) {
            tableBody.innerHTML = '<tr><td colspan="8" class="text-center">Không có phiếu đề xuất nào</td></tr>';
            return;
        }

        tableBody.innerHTML = '';
        requests.forEach(r => {
            // Format Role (nếu là mảng object thì lấy roleName, nếu string thì để nguyên)
            // Ở đây giả định r.role là string (VD: "ROLE_SC_STAFF") hoặc Set<Role>
            // Bạn nói thuộc tính là đúng nên tôi giữ nguyên r.role

            // Badge trạng thái
            let statusBadge = `<span class="badge bg-secondary">${r.status}</span>`;
            if(r.status === 'APPROVED') statusBadge = `<span class="badge bg-success">Đã duyệt</span>`;
            if(r.status === 'REJECTED') statusBadge = `<span class="badge bg-danger">Từ chối</span>`;
            if(r.status === 'PENDING') statusBadge = `<span class="badge bg-warning text-dark">Chờ duyệt</span>`;

            // Nút hành động (Chỉ hiện nút Duyệt/Từ chối nếu là ADMIN và status là PENDING)
            let actionButtons = `<button class="btn-action btn-view" data-id="${r.requestId}">Xem</button>`;

            const isAdmin = userInfo.roles.includes('ROLE_ADMIN');
            if (isAdmin && r.status === 'PENDING') {
                actionButtons += `
                    <button class="btn-action btn-approve bg-success text-white" data-id="${r.requestId}">Duyệt</button>
                    <button class="btn-action btn-reject bg-danger text-white" data-id="${r.requestId}">Từ chối</button>
                `;
            }

            tableBody.innerHTML += `
                <tr>
                    <td>${r.requestId}</td>
                    <td>${r.fullName}</td>
                    <td>${r.username}</td>
                    <td>${r.role || r.roleName || '-'}</td>
                    <td>${r.createdBy}</td>
                    <td>${r.createdAt ? new Date(r.createdAt).toLocaleString('vi-VN') : '-'}</td>
                    <td>${statusBadge}</td>
                    <td>${actionButtons}</td>
                </tr>
            `;
        });

        // Gắn sự kiện
        document.querySelectorAll('.btn-view').forEach(btn => {
            btn.addEventListener('click', e => viewRequest(e.target.dataset.id));
        });

        // Chỉ gắn sự kiện duyệt/từ chối nếu nút đó tồn tại
        document.querySelectorAll('.btn-approve').forEach(btn => {
            btn.addEventListener('click', e => approveRequest(e.target.dataset.id));
        });
        document.querySelectorAll('.btn-reject').forEach(btn => {
            btn.addEventListener('click', e => rejectRequest(e.target.dataset.id));
        });
    }

    function filterRequests(keyword) {
        const filtered = allRequests.filter(r =>
            (r.username && r.username.toLowerCase().includes(keyword)) ||
            (r.fullName && r.fullName.toLowerCase().includes(keyword)) ||
            (r.role && r.role.toString().toLowerCase().includes(keyword))
        );
        renderRequestTable(filtered);
    }

    // 3. Xem chi tiết
    async function viewRequest(requestId) {
        const req = allRequests.find(r => r.requestId == requestId);
        if (!req) return;

        const Swal = window.Swal; // Đảm bảo Swal đã load
        if(Swal) {
            Swal.fire({
                title: `Chi tiết Phiếu #${requestId}`,
                html: `
                    <div style="text-align:left">
                        <p><strong>Họ tên:</strong> ${req.fullName}</p>
                        <p><strong>Username:</strong> ${req.username}</p>
                        <p><strong>Email:</strong> ${req.email}</p>
                        <p><strong>SĐT:</strong> ${req.phone}</p>
                        <p><strong>Vai trò đề xuất:</strong> ${req.proposedRoles || req.role}</p>
                        <p><strong>Người tạo:</strong> ${req.createdBy}</p>
                        <p><strong>Ngày tạo:</strong> ${new Date(req.createdAt).toLocaleString()}</p>
                        <p><strong>Trạng thái:</strong> ${req.status}</p>
                    </div>
                `,
                icon: 'info'
            });
        }
    }

    // 4. Duyệt Phiếu (GỌI API RIÊNG)
    async function approveRequest(requestId) {
        if(!confirm("Bạn chắc chắn muốn DUYỆT phiếu này? User mới sẽ được tạo.")) return;

        try {
            // Gọi POST /approve/{id}
            await api.post(`/staff-requests/approve/${requestId}`);

            if(window.Swal) window.Swal.fire('Thành công', 'Đã duyệt và tạo user mới!', 'success');
            else alert("Đã duyệt thành công!");

            loadRequestList();
        } catch (err) {
            console.error('Lỗi duyệt:', err);
            alert('Lỗi: ' + err.message);
        }
    }

    // 5. Từ Chối Phiếu (GỌI API RIÊNG)
    async function rejectRequest(requestId) {
        if(!confirm("Bạn chắc chắn muốn TỪ CHỐI phiếu này?")) return;

        try {
            // Gọi POST /{id}/reject
            await api.post(`/staff-requests/${requestId}/reject`);

            if(window.Swal) window.Swal.fire('Đã từ chối', 'Phiếu đã bị từ chối.', 'info');
            else alert("Đã từ chối!");

            loadRequestList();
        } catch (err) {
            console.error('Lỗi từ chối:', err);
            alert('Lỗi: ' + err.message);
        }
    }

    // --- INIT ---
    loadRequestList();
}

// Khởi tạo page
initRequestPage();