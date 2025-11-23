import { checkAuth } from './utils/auth.js';
import { renderHeader } from './components/Header.js';
import { renderAdminSidebar } from './components/AdminSidebar.js';
import { getAllUsers, createUser, updateUser, deleteUser } from './services/userService.js';

async function initAdminPage() {
    const userInfo = checkAuth('ROLE_ADMIN');
    if (!userInfo) {
        alert('Bạn không có quyền truy cập!');
        window.location.href = '/login.html';
        return;
    }

    renderHeader();
    renderAdminSidebar();

    const userModal = document.getElementById('user-modal');
    const addUserBtn = document.getElementById('add-user-btn');
    const closeModalBtn = document.getElementById('close-modal-btn');
    const saveUserBtn = document.getElementById('save-user-btn');
    const searchInput = document.getElementById('search-input');

    let editingUserId = null;
    let allUsers = [];

    async function loadUserList() {
        try {
            allUsers = await getAllUsers(); // default ADMIN
            renderUserTable(allUsers);
        } catch (err) {
            alert('Lỗi tải danh sách user: ' + (err.message || 'Không xác định'));
        }
    }

    function renderUserTable(users) {
        const tbody = document.getElementById('user-table-body');
        tbody.innerHTML = '';

        if (!users || !users.length) {
            tbody.innerHTML = '<tr><td colspan="9" style="text-align:center;">Không có user nào</td></tr>';
            return;
        }

        users.forEach(u => {
            const roleBadges = (u.roles || []).map(r => `<span class="role-badge">${r.roleName}</span>`).join(' ');
            tbody.innerHTML += `
                <tr>
                    <td>${u.userId}</td>
                    <td>${u.username}</td>
                    <td class="password-cell">••••••</td>
                    <td>${u.email || ''}</td>
                    <td>${u.phone || ''}</td>
                    <td>${roleBadges}</td>
                    <td>${u.serviceCenterId || ''}</td>
                    <td>${u.status || ''}</td>
                    <td>
                        <button class="btn-action btn-edit" data-id="${u.userId}">Sửa</button>
                        <button class="btn-action btn-delete" data-id="${u.userId}">Xóa</button>
                    </td>
                </tr>
            `;
        });

        document.querySelectorAll('.btn-edit').forEach(btn => {
            btn.addEventListener('click', e => openEditModal(e.target.dataset.id));
        });
        document.querySelectorAll('.btn-delete').forEach(btn => {
            btn.addEventListener('click', e => handleDeleteUser(e.target.dataset.id));
        });
    }

    addUserBtn.addEventListener('click', () => {
        editingUserId = null;
        document.getElementById('modal-title').textContent = 'Thêm Người dùng';
        document.getElementById('user-id').value = '';
        document.getElementById('username').value = '';
        document.getElementById('password').value = '';
        document.getElementById('email').value = '';
        document.getElementById('phone').value = '';
        document.getElementById('role').value = 'USER';
        document.getElementById('status').value = 'ACTIVE';
        userModal.classList.add('show');
    });

    closeModalBtn.addEventListener('click', () => userModal.classList.remove('show'));
    userModal.addEventListener('click', e => { if(e.target===userModal) userModal.classList.remove('show'); });

    saveUserBtn.addEventListener('click', async () => {
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const email = document.getElementById('email').value.trim();
        const phone = document.getElementById('phone').value.trim();
        const roleName = document.getElementById('role').value;
        const status = document.getElementById('status').value;

        if (!username || (!editingUserId && !password)) {
            alert('Username và Password là bắt buộc!');
            return;
        }

        const userData = { username, email, phone, status };
        if (!editingUserId) userData.password = password;

        try {
            if (editingUserId) {
                await updateUser(editingUserId, userData);
                alert('Cập nhật user thành công!');
            } else {
                await createUser(userData, roleName);
                alert('Tạo user thành công!');
            }
            userModal.classList.remove('show');
            await loadUserList(); // reload bảng
        } catch (err) {
            alert('Lỗi lưu user: ' + (err.message || 'Không xác định'));
        }
    });

    function openEditModal(userId) {
        const user = allUsers.find(u => u.userId == userId);
        if (!user) return;

        editingUserId = userId;
        document.getElementById('modal-title').textContent = 'Sửa Người dùng';
        document.getElementById('user-id').value = user.userId;
        document.getElementById('username').value = user.username;
        document.getElementById('password').value = '';
        document.getElementById('email').value = user.email || '';
        document.getElementById('phone').value = user.phone || '';
        const firstRole = user.roles && user.roles.length ? user.roles[0].roleName : 'USER';
        document.getElementById('role').value = firstRole;
        document.getElementById('status').value = user.status || 'ACTIVE';
        userModal.classList.add('show');
    }

    async function handleDeleteUser(userId) {
        if (!confirm('Bạn có chắc muốn xóa user này?')) return;
        try {
            await deleteUser(userId);
            alert('Xóa user thành công!');
            await loadUserList();
        } catch (err) {
            alert('Lỗi xóa user: ' + (err.message || 'Không xác định'));
        }
    }

    searchInput?.addEventListener('input', () => {
        const keyword = searchInput.value.trim().toLowerCase();
        const filtered = allUsers.filter(u =>
            u.username.toLowerCase().includes(keyword) ||
            (u.email || '').toLowerCase().includes(keyword) ||
            (u.phone || '').includes(keyword)
        );
        renderUserTable(filtered);
    });

    await loadUserList();
}

initAdminPage();
