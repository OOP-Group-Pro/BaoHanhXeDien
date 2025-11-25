// src/JS/admin-request.js
import { api } from '../services/apiClient.js';
import { renderAdminSidebar } from '../components/AdminSidebar.js';
renderAdminSidebar();

document.addEventListener('DOMContentLoaded', () => {
    const tableBody = document.getElementById('request-table-body');
    const reloadBtn = document.getElementById('reload-data-btn');
    const searchInput = document.querySelector('.search-bar input');

    let allRequests = [];

    // ==================== LOAD DATA ====================
    async function loadRequestList() {
        tableBody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Đang tải dữ liệu...</td></tr>';
        try {
            const requests = await api.get('/staff-requests/all');
            allRequests = requests;
            renderRequestTable(requests);
        } catch (err) {
            console.error(err);
            tableBody.innerHTML = '<tr><td colspan="8" style="text-align:center; color:red;">Lỗi tải dữ liệu</td></tr>';
        }
    }

    // ==================== RENDER TABLE ====================
    function renderRequestTable(requests) {
        if (!requests.length) {
            tableBody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Không có phiếu đề xuất nào</td></tr>';
            return;
        }

        tableBody.innerHTML = '';
        requests.forEach(r => {
            const row = document.createElement('tr');
            const createdAt = new Date(r.createdAt).toLocaleString();
            const roleHtml = (r.proposedRoles || [])
                .map(role => `<span class="badge-role">${role.name.replace('ROLE_', '')}</span>`).join(' ');

            const processed = ['APPROVED', 'REJECTED'].includes(r.status);

            row.innerHTML = `
                <td>${r.id}</td>
                <td>${r.fullName}</td>
                <td>${r.username}</td>
                <td>${roleHtml}</td>
                <td>${r.createdBy}</td>
                <td>${createdAt}</td>
                <td><span class="badge-status ${r.status.toLowerCase()}">${r.status}</span></td>
                <td class="action-cell">
                    ${processed
                        ? '<button class="btn-view">Xem</button>'
                        : `
                            <button class="btn-edit">Sửa</button>
                            <button class="btn-approve">Duyệt</button>
                            <button class="btn-reject">Từ chối</button>
                        `
                    }
                </td>
            `;

            const td = row.querySelector('.action-cell');

            if (!processed) {
                td.querySelector('.btn-approve').addEventListener('click', () => openModal(r.id, 'approve'));
                td.querySelector('.btn-reject').addEventListener('click', () => openModal(r.id, 'reject'));
                td.querySelector('.btn-edit').addEventListener('click', () => openEditModal(r));
            } else {
                td.querySelector('.btn-view').addEventListener('click', () => viewRequest(r));
            }

            tableBody.appendChild(row);
        });
    }

    // ==================== MODAL Duyệt / Từ chối ====================
    function openModal(requestId, type) {
        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.style.cssText = `
            position: fixed; top:0; left:0; width:100%; height:100%;
            background: rgba(0,0,0,0.5); display:flex; justify-content:center; align-items:center; z-index:9999;
        `;
        modal.innerHTML = `
            <div class="modal-content" style="
                background:white; padding:20px; border-radius:8px;
                width:400px; max-width:90%; text-align:left; position:relative;
            ">
                <h3 style="color:${type==='approve'?'#27ae60':'#e74c3c'}; margin-bottom:15px;">
                    ${type==='approve'? 'Duyệt Phiếu' : 'Từ chối Phiếu'}
                </h3>
                <p>Bạn có chắc chắn muốn ${type==='approve'? 'duyệt' : 'từ chối'} phiếu <b>${requestId}</b> này?</p>
                ${type==='reject'? '<textarea id="reason" placeholder="Nhập lý do từ chối..." style="width:100%; margin-top:10px; padding:8px; border-radius:4px; border:1px solid #ccc;"></textarea>' : ''}
                <div style="margin-top:20px; text-align:right;">
                    <button class="btn-cancel" style="background:#bdc3c7;color:white;padding:6px 12px;border:none;border-radius:4px;margin-right:10px;cursor:pointer;">Hủy</button>
                    <button class="btn-confirm" style="background:${type==='approve'?'#27ae60':'#e74c3c'};color:white;padding:6px 12px;border:none;border-radius:4px;cursor:pointer;">
                        ${type==='approve'? 'Xác nhận Duyệt' : 'Xác nhận Từ chối'}
                    </button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);

        modal.querySelector('.btn-cancel').addEventListener('click', () => modal.remove());
        modal.querySelector('.btn-confirm').addEventListener('click', async () => {
            const reason = type==='reject' ? modal.querySelector('#reason').value : null;
            await handleAction(requestId, type, reason);
            modal.remove();
        });
    }

    async function handleAction(requestId, action, reason = null) {
        try {
            if (action === 'approve') {
                await api.post(`/staff-requests/approve/${requestId}`);
                Swal.fire('Thành công', `Phiếu ${requestId} đã được duyệt`, 'success');
            } else {
                await api.post(`/staff-requests/${requestId}/reject`, { reason });
                Swal.fire('Thành công', `Phiếu ${requestId} đã bị từ chối`, 'success');
            }
            loadRequestList();
        } catch (err) {
            console.error(err);
            Swal.fire('Lỗi', `Không thể ${action==='approve'?'duyệt':'từ chối'} phiếu`, 'error');
        }
    }

    // ==================== EDIT MODAL ====================
    function openEditModal(request) {
        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.style.cssText = `
            position:fixed; top:0; left:0; width:100%; height:100%;
            background:rgba(0,0,0,0.5); display:flex; justify-content:center; align-items:center; z-index:9999;
        `;
        modal.innerHTML = `
            <div class="modal-content" style="
                background:white; padding:20px; border-radius:8px;
                width:400px; max-width:90%; text-align:left; position:relative;
            ">
                <h3 style="color:#3498db; margin-bottom:15px;">Sửa Phiếu ${request.id}</h3>
                <input id="edit-fullName" class="swal2-input" placeholder="Full Name" value="${request.fullName}" style="margin-bottom:10px;">
                <input id="edit-username" class="swal2-input" placeholder="Username" value="${request.username}" style="margin-bottom:10px;">
                <input id="edit-role" class="swal2-input" placeholder="Role (ROLE_...)" value="${(request.proposedRoles||[]).map(r=>r.name).join(', ')}" style="margin-bottom:10px;">
                <div style="margin-top:20px; text-align:right;">
                    <button class="btn-cancel" style="background:#bdc3c7;color:white;padding:6px 12px;border:none;border-radius:4px;margin-right:10px;cursor:pointer;">Hủy</button>
                    <button class="btn-confirm" style="background:#3498db;color:white;padding:6px 12px;border:none;border-radius:4px;cursor:pointer;">Lưu</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);

        modal.querySelector('.btn-cancel').addEventListener('click', () => modal.remove());
        modal.querySelector('.btn-confirm').addEventListener('click', async () => {
            const updatedRequest = {
                fullName: modal.querySelector('#edit-fullName').value,
                username: modal.querySelector('#edit-username').value,
                proposedRoles: modal.querySelector('#edit-role').value.split(',').map(r=>({name:r.trim()}))
            };
            try {
                await api.put(`/staff-requests/${request.id}`, updatedRequest);
                Swal.fire('Thành công', 'Phiếu đã được cập nhật', 'success');
                modal.remove();
                loadRequestList();
            } catch(err) {
                console.error(err);
                Swal.fire('Lỗi', 'Không thể cập nhật phiếu', 'error');
            }
        });
    }

    // ==================== VIEW ====================
    function viewRequest(request) {
        Swal.fire({
            title: `Chi tiết Phiếu ${request.id}`,
            html: `
                <p><b>Full Name:</b> ${request.fullName}</p>
                <p><b>Username:</b> ${request.username}</p>
                <p><b>Roles:</b> ${(request.proposedRoles||[]).map(r=>r.name).join(', ')}</p>
                <p><b>Status:</b> ${request.status}</p>
                <p><b>Created By:</b> ${request.createdBy}</p>
                <p><b>Created At:</b> ${new Date(request.createdAt).toLocaleString()}</p>
            `,
            icon: 'info'
        });
    }

    // ==================== SEARCH ====================
    searchInput.addEventListener('input', () => {
        const keyword = searchInput.value.toLowerCase();
        const filtered = allRequests.filter(r =>
            r.fullName.toLowerCase().includes(keyword) ||
            r.username.toLowerCase().includes(keyword) ||
            (r.proposedRoles||[]).some(role => role.name.toLowerCase().includes(keyword))
        );
        renderRequestTable(filtered);
    });

    // ==================== RELOAD ====================
    if (reloadBtn) reloadBtn.addEventListener('click', loadRequestList);

    // ==================== INIT ====================
    loadRequestList();
});
