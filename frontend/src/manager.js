// src/manager.js
import { checkAuth, logout } from './utils/auth.js';
import { api } from './services/apiClient.js';

async function initManagerPage() {
    // Kiểm tra quyền manager
    const userInfo = checkAuth('ROLE_MANAGER');
    if (!userInfo) {
        alert('Bạn không có quyền truy cập!');
        window.location.href = '/login.html';
        return;
    }

    const logoutBtn = document.getElementById('logout-btn');
    const createBtn = document.getElementById('create-request-btn');
    const saveBtn = document.getElementById('save-request-btn');
    const closeModalBtn = document.getElementById('close-request-modal');
    const modal = document.getElementById('request-modal');
    const tableBody = document.getElementById('request-table-body');

    const fullNameInput = document.getElementById('fullName');
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    const phoneInput = document.getElementById('phone');
    const roleSelect = document.getElementById('role');
    const requestIdInput = document.getElementById('request-id');

    let allRequests = [];

    // --- EVENTS ---
    logoutBtn.addEventListener('click', () => {
        logout();
        window.location.href = '/login.html';
    });

    createBtn.addEventListener('click', () => openModal());

    closeModalBtn.addEventListener('click', () => closeModal());

    saveBtn.addEventListener('click', async () => {
        const fullName = fullNameInput.value.trim();
        const username = usernameInput.value.trim();
        const email = emailInput.value.trim();
        const phone = phoneInput.value.trim();
        const roleName = roleSelect.value;

        if (!fullName || !username || !roleName) {
            Swal.fire('Lỗi', 'Full name, username và role là bắt buộc!', 'warning');
            return;
        }

        const requestData = { fullName, username, email, phone, roleName };

        try {
            if (requestIdInput.value) {
                // Update phiếu (nếu cần)
                await api.put(`/requests/new-staff/${requestIdInput.value}`, requestData);
                Swal.fire('Thành công', 'Phiếu đã được cập nhật', 'success');
            } else {
                // Tạo phiếu mới
                await api.post('/requests/new-staff', requestData);
                Swal.fire('Thành công', 'Phiếu mới đã được tạo', 'success');
            }
            closeModal();
            loadRequestList();
        } catch (err) {
            console.error(err);
            Swal.fire('Lỗi', 'Không thể lưu phiếu', 'error');
        }
    });

    // --- FUNCTIONS ---
    function openModal(request = null) {
        if (request) {
            requestIdInput.value = request.requestId;
            fullNameInput.value = request.fullName;
            usernameInput.value = request.username;
            emailInput.value = request.email || '';
            phoneInput.value = request.phone || '';
            roleSelect.value = request.roleName;
            document.getElementById('modal-title').textContent = 'Chỉnh sửa Phiếu';
        } else {
            requestIdInput.value = '';
            fullNameInput.value = '';
            usernameInput.value = '';
            emailInput.value = '';
            phoneInput.value = '';
            roleSelect.value = 'ROLE_USER';
            document.getElementById('modal-title').textContent = 'Tạo Phiếu Mới';
        }
        modal.style.display = 'flex';
    }

    function closeModal() {
        modal.style.display = 'none';
    }

    async function loadRequestList() {
        tableBody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Đang tải dữ liệu...</td></tr>';
        try {
            const requests = await api.get('/requests/new-staff'); // API backend của bạn
            allRequests = requests;
            renderTable(requests);
        } catch (err) {
            console.error(err);
            tableBody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Lỗi tải dữ liệu</td></tr>';
        }
    }

    function renderTable(requests) {
        if (!requests.length) {
            tableBody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Không có phiếu nào</td></tr>';
            return;
        }

        tableBody.innerHTML = '';
        requests.forEach(r => {
            tableBody.innerHTML += `
                <tr>
                    <td>${r.requestId}</td>
                    <td>${r.fullName}</td>
                    <td>${r.username}</td>
                    <td>${r.email || ''}</td>
                    <td>${r.phone || ''}</td>
                    <td>${r.roleName}</td>
                    <td>${r.status}</td>
                    <td>
                        <button class="btn-action btn-edit" data-id="${r.requestId}">Sửa</button>
                    </td>
                </tr>
            `;
        });

        document.querySelectorAll('.btn-edit').forEach(btn => {
            btn.addEventListener('click', e => {
                const req = allRequests.find(r => r.requestId == e.target.dataset.id);
                openModal(req);
            });
        });
    }

    // INIT
    loadRequestList();
}

// Khởi tạo page
initManagerPage();
