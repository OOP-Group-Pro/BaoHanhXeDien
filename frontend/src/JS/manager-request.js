// src/request.js

import { logout } from "../services/authService.js";
import { checkAuth } from '../utils/auth.js';
import { api } from '../services/apiClient.js';

async function initRequestPage() {
    // Kiểm tra quyền ADMIN
    const userInfo = checkAuth('ROLE_ADMIN');
    if (!userInfo) {
        alert('Bạn không có quyền truy cập!');
        window.location.href = '/login.html';
        return;
    }

    const logoutBtn = document.getElementById('logout-btn');
    const reloadBtn = document.getElementById('reload-data-btn');
    const searchInput = document.querySelector('.search-bar input');
    const tableBody = document.getElementById('request-table-body');

    // --- EVENTS ---
    logoutBtn.addEventListener('click', () => {
        logout();
        window.location.href = '/login.html';
    });

    reloadBtn.addEventListener('click', loadRequestList);

    searchInput.addEventListener('input', () => {
        const keyword = searchInput.value.trim().toLowerCase();
        filterRequests(keyword);
    });

    let allRequests = [];

    // --- FUNCTIONS ---
    async function loadRequestList() {
        tableBody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Đang tải dữ liệu...</td></tr>';
        try {
            // API giả sử: /requests/new-staff
            const requests = await api.get('/requests/new-staff');
            allRequests = requests;
            renderRequestTable(requests);
        } catch (err) {
            console.error('Lỗi tải dữ liệu phiếu đề xuất:', err);
            tableBody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Lỗi tải dữ liệu</td></tr>';
        }
    }

    function renderRequestTable(requests) {
        if (!requests.length) {
            tableBody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Không có phiếu đề xuất nào</td></tr>';
            return;
        }

        tableBody.innerHTML = '';
        requests.forEach(r => {
            tableBody.innerHTML += `
                <tr>
                    <td>${r.requestId}</td>
                    <td>${r.fullName}</td>
                    <td>${r.username}</td>
                    <td>${r.roleName}</td>
                    <td>${r.createdBy}</td>
                    <td>${new Date(r.createdAt).toLocaleString()}</td>
                    <td>${r.status}</td>
                    <td>
                        <button class="btn-action btn-view" data-id="${r.requestId}">Xem</button>
                        <button class="btn-action btn-approve" data-id="${r.requestId}">Duyệt</button>
                        <button class="btn-action btn-reject" data-id="${r.requestId}">Từ chối</button>
                    </td>
                </tr>
            `;
        });

        // Event cho từng hành động
        document.querySelectorAll('.btn-view').forEach(btn => {
            btn.addEventListener('click', e => viewRequest(e.target.dataset.id));
        });
        document.querySelectorAll('.btn-approve').forEach(btn => {
            btn.addEventListener('click', e => updateRequestStatus(e.target.dataset.id, 'APPROVED'));
        });
        document.querySelectorAll('.btn-reject').forEach(btn => {
            btn.addEventListener('click', e => updateRequestStatus(e.target.dataset.id, 'REJECTED'));
        });
    }

    function filterRequests(keyword) {
        const filtered = allRequests.filter(r =>
            r.username.toLowerCase().includes(keyword) ||
            r.fullName.toLowerCase().includes(keyword) ||
            r.roleName.toLowerCase().includes(keyword)
        );
        renderRequestTable(filtered);
    }

    async function viewRequest(requestId) {
        const req = allRequests.find(r => r.requestId == requestId);
        if (!req) return;

        Swal.fire({
            title: `Chi tiết Phiếu ${requestId}`,
            html: `
                <p><strong>Full Name:</strong> ${req.fullName}</p>
                <p><strong>Username:</strong> ${req.username}</p>
                <p><strong>Role:</strong> ${req.roleName}</p>
                <p><strong>Status:</strong> ${req.status}</p>
                <p><strong>Created By:</strong> ${req.createdBy}</p>
                <p><strong>Created At:</strong> ${new Date(req.createdAt).toLocaleString()}</p>
            `,
            icon: 'info'
        });
    }

    async function updateRequestStatus(requestId, status) {
        try {
            await api.put(`/requests/new-staff/${requestId}/status`, { status });
            Swal.fire('Thành công', `Phiếu ${requestId} đã được cập nhật thành ${status}`, 'success');
            loadRequestList();
        } catch (err) {
            console.error('Lỗi cập nhật trạng thái:', err);
            Swal.fire('Lỗi', 'Không thể cập nhật trạng thái phiếu', 'error');
        }
    }

    // --- INIT ---
    loadRequestList();
}

// Khởi tạo page
initRequestPage();
