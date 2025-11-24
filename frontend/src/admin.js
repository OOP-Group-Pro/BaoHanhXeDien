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
    // Lấy thêm các element bộ lọc mới
    const filterRole = document.getElementById('filter-role');
    const filterCenter = document.getElementById('filter-center');
    const filterStatus = document.getElementById('filter-status');
    const btnClearFilter = document.getElementById('btn-clear-filter');

    let editingUserId = null;
    let allUsers = [];

    async function loadUserList() {
        try {
            allUsers = await getAllUsers(); // default ADMIN
            console.log("👀 called getAllUsers(): ", allUsers);
            renderUserTable(allUsers.content);
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
            const roleBadges = (u.roles || []).map(r =>
                `<span class="role-badge">${r.roleName || r}</span>`
            ).join(' ');
            const initial = u.username ? u.username.charAt(0).toUpperCase() : '?';
            const avatarColor = getRandomColor();

            // 3. Xử lý Last Login
            let lastLoginDisplay = '<span class="text-muted" style="font-size:11px">Chưa đăng nhập</span>';
            if (u.lastLogin) {
                const date = new Date(u.lastLogin);
                lastLoginDisplay = `<span style="font-size:12px; color:#555">${date.toLocaleDateString('vi-VN')} ${date.toLocaleTimeString('vi-VN', {hour: '2-digit', minute:'2-digit'})}</span>`;
            }

            console.log(`👀👀 Last login of ${u.username} at ${u.lastLogin}`);

            // 4. Xử lý Status
            let statusDotClass = 'status-dot-grey';
            let statusText = u.status || 'Unknown';
            if (u.status === 'ACTIVE') statusDotClass = 'status-dot-green';
            else if (u.status === 'LOCKED' || u.status === 'BLOCKED') statusDotClass = 'status-dot-red';

            // 5. Render HTML (ĐÃ SỬA LỖI TABLE)
            tbody.innerHTML += `
            <tr>
                <td>${u.userId}</td>
                <td>
                    <div class="user-info-cell">
                        <div class="user-avatar" style="background-color: ${avatarColor}">${initial}</div>
                        <div>
                            <div class="fw-bold" style="color:#007bff">${u.username}</div>
                            ${lastLoginDisplay}
                        </div>
                    </div>
                </td>
                <td>${u.email || '-'}</td>
                <td>${u.phone || '-'}</td> <td>${roleBadges}</td>
                <td class="text-center">${u.serviceCenterId || '<span class="badge bg-secondary">HQ</span>'}</td>
                
                <td>
                    <div class="status-container">
                        <span class="status-dot ${statusDotClass}"></span>
                        <span class="status-text">${statusText}</span>
                    </div>
                </td>

                <td>
                    <button class="btn-action btn-reset-pass" title="Reset Pass" data-id="${u.userId}" style="background:#17a2b8;">
                        <i class="fa-solid fa-key" style="pointer-events: none;"></i> </button>
                    <button class="btn-action btn-edit" title="Sửa" data-id="${u.userId}" style="background:#ffc107; color:#000">
                        <i class="fa-solid fa-pen" style="pointer-events: none;"></i>
                    </button>
                    <button class="btn-action btn-delete" title="Xóa" data-id="${u.userId}" style="background:#dc3545; color:#fff">
                        <i class="fa-solid fa-trash" style="pointer-events: none;"></i>
                    </button>
                </td>
            </tr>             
            `;
        });

        // 🔥 SỬA LỖI CLICK UNDEFINED (Dùng currentTarget)
        document.querySelectorAll('.btn-edit').forEach(btn => {
            btn.addEventListener('click', e => openEditModal(e.currentTarget.dataset.id));
        });
        document.querySelectorAll('.btn-delete').forEach(btn => {
            btn.addEventListener('click', e => handleDeleteUser(e.currentTarget.dataset.id));
        });
        document.querySelectorAll('.btn-reset-pass').forEach(btn => {
            btn.addEventListener('click', e => handleResetPass(e.currentTarget.dataset.id));
        });
    }

    // 🔥 HÀM LỌC DỮ LIỆU CHÍNH (LOGIC MỚI)
    function applyFilters() {
        const keyword = searchInput.value.trim().toLowerCase();
        const roleVal = filterRole.value;
        const centerVal = filterCenter.value.trim();
        const statusVal = filterStatus.value;

        // Lọc từ danh sách gốc (allUsers.content)
        // Lưu ý: Backend trả về Page object nên data nằm trong .content
        const sourceData = allUsers.content || [];

        const filtered = sourceData.filter(u => {
            // 1. Check Từ khóa (Username, Email, Phone)
            const matchKeyword = !keyword ||
                u.username.toLowerCase().includes(keyword) ||
                (u.email && u.email.toLowerCase().includes(keyword)) ||
                (u.phone && u.phone.includes(keyword));

            // 2. Check Vai trò (Roles là mảng object)
            // u.roles = [{roleName: "ADMIN"}, ...]
            const matchRole = !roleVal ||
                (u.roles && u.roles.some(r => r.roleName === roleVal));

            // 3. Check Trung tâm (So sánh ID)
            // Dùng == thay vì === để so sánh chuỗi "1" với số 1
            const matchCenter = !centerVal ||
                (u.serviceCenterId == centerVal);

            // 4. Check Trạng thái
            const matchStatus = !statusVal ||
                (u.status === statusVal);

            // Trả về true nếu thỏa mãn TẤT CẢ điều kiện
            return matchKeyword && matchRole && matchCenter && matchStatus;
        });

        renderUserTable(filtered);
    }

    // Gắn sự kiện cho tất cả các ô input/select
    // 'input' chạy ngay khi gõ, 'change' chạy khi chọn select
    searchInput.addEventListener('input', applyFilters);
    filterRole.addEventListener('change', applyFilters);
    filterCenter.addEventListener('input', applyFilters);
    filterStatus.addEventListener('change', applyFilters);

    // Nút Reset bộ lọc
    btnClearFilter.addEventListener('click', () => {
        searchInput.value = '';
        filterRole.value = '';
        filterCenter.value = '';
        filterStatus.value = '';
        applyFilters(); // Gọi lại để render full bảng
    });

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

    // Hàm phụ tạo màu ngẫu nhiên cho Avatar
    function getRandomColor() {
        const colors = ['#007bff', '#6610f2', '#6f42c1', '#e83e8c', '#dc3545', '#fd7e14', '#28a745', '#20c997', '#17a2b8'];
        return colors[Math.floor(Math.random() * colors.length)];
    }

    function openEditModal(userId) {
        const user = allUsers.content.find(u => u.userId == userId);
        if (!user) {
            console.error("❌ User not found with ID: ", userId);
            return;
        }

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

    // --- LOGIC RESET MẬT KHẨU (MỞ RỘNG ĐƯỢC) ---
    async function handleResetPass(userId) {
        // 1. Xác nhận hành động
        const confirmed = confirm('⚠️ CẢNH BÁO: Bạn có chắc muốn reset mật khẩu của user này về mặc định (Abc@12345)?\n\n(Sau này tính năng này sẽ gửi email kích hoạt lại cho nhân viên)');

        if (!confirmed) return;

        // 2. Tìm thông tin user (để lấy username hiển thị cho chắc)
        const user = allUsersOriginal.find(u => u.userId == userId);
        if (!user) {
            alert("Lỗi: Không tìm thấy user ID: " + userId);
            return;
        }

        try {
            // 3. Gọi API cập nhật (Hiện tại: Update trực tiếp)
            // Tương lai: Thay bằng api.post('/auth/reset-password-request', { email: user.email })

            const defaultPass = "Abc@12345"; // Mật khẩu mặc định quy ước

            // Tận dụng hàm updateUser đã có để đổi pass
            await updateUser(userId, {
                password: defaultPass,
                // Gửi kèm các trường bắt buộc khác nếu backend yêu cầu (tùy logic update của bạn)
                // Nhưng thường API update nên cho phép gửi partial data (PATCH)
            });

            // 4. Thông báo kết quả
            alert(`✅ Đã reset mật khẩu cho user [${user.username}] thành công!\nMật khẩu mới là: ${defaultPass}`);

        } catch (err) {
            console.error("Lỗi reset pass:", err);
            alert('❌ Lỗi khi reset: ' + (err.message || 'Lỗi hệ thống'));
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