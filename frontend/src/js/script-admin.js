import { api as apiClient } from '../../src/services/apiClient.js';
import { renderManagerSidebar } from '../components/ManagerSidebar.js';
renderManagerSidebar();
document.addEventListener("DOMContentLoaded", () => {
    const path = location.pathname.toLowerCase();
    const currentPage = path.split('/').pop();

    const isDashboard = path.includes("index");
    const isUsersPage = path.includes("users");
    const isRequestsPage = path.endsWith("request.html");
    const isInventoryPage = path.includes("inventory");
    const isClaimsPage = path.includes("claims");
    const isCreateRequestPage = path.endsWith("create-request.html");


    const Swal = window.Swal;

    // ================= GLOBAL FUNCTIONS =================
    const mockDecodeToken = (token) => {
        if (token === "manager123") return { username: "manager1", roles: ["ROLE_USER", "ROLE_MANAGER"] };
        return { username: "user1", roles: ["ROLE_USER"] };
    };

    const createBadge = (value, type = 'status') => {
        let className = '', text = value;
        if (type === 'status') {
            switch (value) {
                case 'PENDING': className = 'badge-pending'; text = 'Đang chờ duyệt'; break;
                case 'APPROVED': className = 'badge-approved'; text = 'Đã duyệt'; break;
                case 'REJECTED': className = 'badge-rejected'; text = 'Đã từ chối'; break;
                case 'ACTIVE': className = 'badge-approved'; text = 'Hoạt động'; break;
                case 'LOCKED': className = 'badge-rejected'; text = 'Khóa'; break;
                default: className = 'badge-pending'; text = 'Không hoạt động'; break;
            }
        } else if (type === 'role') {
            const roleName = value.toLowerCase();
            if (roleName.includes('admin')) className = 'badge-admin';
            else if (roleName.includes('manager')) className = 'badge-manager';
            else if (roleName.includes('technician') || roleName.includes('staff')) className = 'badge-user';
            else className = 'badge-default';
            text = value.replace('ROLE_', '').replace('_', ' ').trim();
        }
        return `<span class="badge ${className}">${text}</span>`;
    };

    const getBadgeClass = (status) => {
        switch (status) {
            case 'Đang chờ': return 'badge-pending';
            case 'Đã duyệt': return 'badge-approved';
            case 'Từ chối': return 'badge-rejected';
            default: return '';
        }
    };

    // ================= GLOBAL: LOGOUT =================


// Logout
    const logoutBtn = document.getElementById('logout-btn');

    // Đảm bảo script SweetAlert2 được tải trước khi chạy
    //const Swal = window.Swal;

    if (logoutBtn && Swal) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault(); // Ngăn hành động mặc định của thẻ <a>

            Swal.fire({
                title: "Xác nhận Đăng xuất",
                text: "Bạn có chắc chắn muốn thoát khỏi hệ thống?",
                icon: "warning",
                showCancelButton: true,
                confirmButtonColor: "#3085d6",
                cancelButtonColor: "#d33",
                confirmButtonText: "Đồng ý, Đăng xuất!",
                cancelButtonText: "Hủy bỏ"
            }).then((result) => {
                if (result.isConfirmed) {
                    localStorage.clear()
                    Swal.fire({
                        title: "Đã đăng xuất!",
                        text: "Bạn đã thoát khỏi hệ thống thành công.",
                        icon: "success",
                        showConfirmButton: false,
                        timer: 1500 // Tự động đóng sau 1.5 giây
                    }).then(() => {
                        window.location.href = '../../index.html';
                    });
                }
            });
        });
    }
// ================= GLOBAL: SIDEBAR ACTIVE =================
    const links = document.querySelectorAll('.sidebar-nav a');
    const currentPath = window.location.pathname.split('/').pop().toLowerCase();

    links.forEach(link => {
        const href = link.getAttribute('href')?.toLowerCase();
        if (!href) return;

        if (currentPath.includes(href) && href !== '#') {
            links.forEach(l => l.classList.remove('active'));
            link.classList.add('active');
        }

        if ((currentPath === '' || currentPath === 'index.html') && href.includes('index')) {
            links.forEach(l => l.classList.remove('active'));
            link.classList.add('active');
        }
    });

    // ================= GLOBAL: MANAGER LINK =================
    const managerLink = document.getElementById('manager-link');
    if (managerLink) {
        managerLink.addEventListener('click', e => {
            e.preventDefault();
            const token = prompt("Nhập token Manager:");
            if (!token) {
                alert("Bạn chưa nhập token!");
                return;
            }
            const decoded = mockDecodeToken(token);
            if (decoded.roles.includes('ROLE_MANAGER')) {
                alert("Xác thực Manager thành công! Chuyển sang tạo request...");
                window.location.href = 'create-request.html'; // Default; adjust per page if needed
            } else {
                alert("Token không có quyền Manager!");
            }
        });
    }

    // ================= CLAIMS PAGE =================
    if (currentPage === 'claims.html') {
        const urlParams = new URLSearchParams(window.location.search);
        const tab = urlParams.get('tab');
        if (tab === 'claim') {
            const claimLabel = document.getElementById('claim-sub-label');
            if (claimLabel) claimLabel.classList.add('active');
        }

        const tbody = document.querySelector('#claims-table tbody');
        const API_BASE_URL = 'http://localhost:8004/api/v1';

        async function fetchClaimsData() {
            console.log("Đang gọi API: GET /api/v1/claims");
            try {
                const response = await fetch(`${API_BASE_URL}/claims`);
                if (!response.ok) {
                    throw new Error(`Mạng không ổn định hoặc API lỗi: ${response.statusText}`);
                }
                const data = await response.json();
                console.log("Dữ liệu nhận được:", data);
                return data;
            } catch (error) {
                console.error("Lỗi khi fetch dữ liệu:", error);
                if (tbody) {
                    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--danger-red); padding: 40px;">Lỗi khi tải dữ liệu: ${error.message}</td></tr>`;
                }
                return [];
            }
        }

        function populateClaimsTable(data) {
            if (tbody) {
                tbody.innerHTML = '';
                if (!data || data.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 40px;">Không tìm thấy dữ liệu claims.</td></tr>';
                    return;
                }
                data.forEach(claim => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>${claim.claimCode}</td>
                        <td>${claim.vin}</td>
                        <td>${claim.dateCreated}</td>
                        <td><span class="badge ${getBadgeClass(claim.currentStatus)}">${claim.currentStatus}</span></td>
                        <td>${claim.description}</td>
                        <td><button class="btn-icon btn-view" data-id="${claim.id}" title="Xem chi tiết"><i class="fa-solid fa-eye"></i></button></td>
                    `;
                    tbody.appendChild(tr);
                });

                document.querySelectorAll('.btn-view').forEach(btn => {
                    btn.addEventListener('click', e => {
                        const claimId = e.currentTarget.dataset.id;
                        if (Swal) {
                            Swal.fire('Chi tiết Yêu cầu', `Yêu cầu ID: ${claimId}`, 'info');
                        } else {
                            alert(`Chi tiết Yêu cầu ID: ${claimId}`);
                        }
                    });
                });
            }
        }

        async function loadAndDisplayClaims() {
            const data = await fetchClaimsData();
            populateClaimsTable(data);
        }

        loadAndDisplayClaims();
    }

    // ================= CREATE-REQUEST PAGE =================
    if (currentPage === 'create-request.html') {
        const tableBody = document.getElementById('request-table-body');
        const modal = document.getElementById('request-modal');
        const modalTitle = document.getElementById('modal-title');
        const saveBtn = document.getElementById('save-request-btn');
        const closeModalBtn = document.getElementById('close-request-modal');
        const createBtn = document.getElementById('create-request-btn');

        // --- 1. LOAD DANH SÁCH (CHỈ CỦA TÔI) ---
        const loadRequests = async () => {
            if (!tableBody) return;
            tableBody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Đang tải dữ liệu...</td></tr>';

            try {
                // 🔥 Gọi API lấy phiếu của mình
                const response = await apiClient.get('/staff-requests/my');
                const requests = response.data || response;
                renderTable(requests);
            } catch (error) {
                console.error(error);
                tableBody.innerHTML = '<tr><td colspan="8" style="text-align:center; color:red;">Lỗi tải dữ liệu</td></tr>';
            }
        };

        // --- 2. RENDER BẢNG (KHÔNG NÚT SỬA/XÓA) ---
        const renderTable = (requests) => {
            tableBody.innerHTML = '';

            if (!requests || requests.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Chưa có phiếu nào</td></tr>';
                return;
            }

            requests.forEach(r => {
                const row = document.createElement('tr');

                // 🔥 Xử lý Role hiển thị
                let roleDisplay = r.role || r.roleName || (r.proposedRoles ? (Array.isArray(r.proposedRoles) ? r.proposedRoles[0] : r.proposedRoles) : "Chưa cấp");
                if (typeof roleDisplay === 'string') roleDisplay = roleDisplay.replace('ROLE_', '');

                const phoneNumber = r.phone || r.phoneNumber || "";
                const id = r.requestId || r.id;

                row.innerHTML = `
                    <td>${id}</td>
                    <td>${r.fullName}</td>
                    <td>${r.username}</td>
                    <td>${r.email}</td>
                    <td>${phoneNumber}</td>
                    <td><span class="badge badge-info" style="font-weight:bold;">${roleDisplay}</span></td>
                    <td>${createBadge(r.status)}</td>
                    <td>
                        <span style="color: #888; font-size: 0.85rem; font-style: italic;">Đã gửi</span>
                    </td>`;
                tableBody.appendChild(row);
            });
        };

        // --- 3. TẠO PHIẾU MỚI ---
        if (createBtn) {
            createBtn.addEventListener('click', () => {
                if (!modal) return;
                modalTitle.textContent = 'Tạo Phiếu Đề Xuất Mới';
                modal.style.display = 'flex';
                document.getElementById('request-id').value = '';
                document.getElementById('fullName').value = '';
                document.getElementById('username').value = '';
                document.getElementById('email').value = '';
                document.getElementById('phone').value = '';
                document.getElementById('role').value = 'ROLE_USER';
            });
        }

        if (saveBtn) {
            saveBtn.addEventListener('click', async () => {
                const requestData = {
                    fullName: document.getElementById('fullName').value,
                    username: document.getElementById('username').value,
                    email: document.getElementById('email').value,
                    phone: document.getElementById('phone').value,
                    role: document.getElementById('role').value, // 🔥 Gửi Role đi
                };

                if (!requestData.username || !requestData.email) {
                    Swal.fire('Lỗi', 'Vui lòng điền đủ thông tin', 'warning');
                    return;
                }

                try {
                    saveBtn.innerText = 'Đang gửi...';
                    saveBtn.disabled = true;
                    // 🔥 Gọi API tạo mới
                    await apiClient.post('/staff-requests/create', requestData);

                    Swal.fire('Thành công', 'Đã gửi phiếu đề xuất!', 'success');
                    if (modal) modal.style.display = 'none';
                    loadRequests(); // Load lại
                } catch (error) {
                    console.error(error);
                    Swal.fire('Thất bại', 'Lỗi server', 'error');
                } finally {
                    saveBtn.innerText = 'Lưu';
                    saveBtn.disabled = false;
                }
            });
        }

        // --- 4. ĐĂNG XUẤT ---
        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', (e) => {
                e.preventDefault();
                Swal.fire({
                    title: "Đăng xuất?", text: "Bạn chắc chắn muốn thoát?", icon: "warning",
                    showCancelButton: true, confirmButtonText: "Đăng xuất"
                }).then((result) => {
                    if (result.isConfirmed) {
                        localStorage.removeItem('accessToken');
                        localStorage.removeItem('refreshToken');
                        localStorage.removeItem('user');
                        window.location.href = '../../index.html';
                    }
                });
            });
        }

        // Modal close logic...
        if (closeModalBtn) closeModalBtn.addEventListener('click', () => { if (modal) modal.style.display = 'none'; });
        window.onclick = function(event) { if (event.target === modal) modal.style.display = 'none'; };

        // Khởi chạy
        loadRequests();
    }

    // ================= INVENTORY PAGE =================
    if (currentPage === 'inventory.html') {
        const tableBody = document.getElementById('inventory-table-body');
        const API_BASE_URL = 'http://localhost:8004/api/v1';

        // Modals and forms
        const addPartModal = document.getElementById('add-part-modal');
        const addPartBtn = document.getElementById('add-new-part-btn');
        const closePartModalBtn = document.getElementById('close-part-modal');
        const cancelPartModalBtn = document.getElementById('cancel-part-modal');
        const addPartForm = document.getElementById('add-part-form');
        const partErrorMessage = document.getElementById('part-error-message');

        const addStockModal = document.getElementById('add-stock-modal');
        const addStockBtn = document.getElementById('add-stock-btn');
        const closeStockModalBtn = document.getElementById('close-stock-modal');
        const cancelStockModalBtn = document.getElementById('cancel-stock-modal');
        const addStockForm = document.getElementById('add-stock-form');
        const stockErrorMessage = document.getElementById('stock-error-message');

        async function fetchInventoryData() {
            console.log("Đang gọi API: GET /api/v1/inventory/all-stock");
            try {
                const response = await fetch(`${API_BASE_URL}/inventory/all-stock`);
                if (!response.ok) throw new Error(`Mạng không ổn định hoặc API lỗi: ${response.statusText}`);
                return await response.json();
            } catch (error) {
                console.error("Lỗi khi fetch dữ liệu:", error);
                if (tableBody) tableBody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: var(--danger-red); padding: 40px;">Lỗi khi tải dữ liệu: ${error.message}</td></tr>`;
                return [];
            }
        }

        function populateTable(data) {
            if (!tableBody) return;
            tableBody.innerHTML = '';
            if (!data || data.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="3" style="text-align: center; padding: 40px;">Không tìm thấy dữ liệu tồn kho.</td></tr>';
                return;
            }
            data.forEach(item => {
                const row = document.createElement('tr');
                row.setAttribute('data-id', item.inventoryId);
                row.innerHTML = `
                    <td>
                        <span class="part-name">${item.partName}</span>
                        <span class="part-id">Part ID: ${item.partId}</span>
                    </td>
                    <td>${item.location}</td>
                    <td>
                        <div class="quantity-cell">
                            <input type="number" class="quantity-input" value="${item.quantity}" readonly>
                            <button class="edit-btn" title="Chỉnh sửa số lượng">
                                <i class="fa-solid fa-pencil"></i>
                            </button>
                        </div>
                    </td>
                `;
                tableBody.appendChild(row);
            });
        }

        async function updateQuantity(inventoryId, newQuantity) {
            console.log(`Đang gọi API: PATCH /api/v1/inventory/${inventoryId}/quantity`);
            try {
                const response = await fetch(`${API_BASE_URL}/inventory/${inventoryId}/quantity`, {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ quantity: newQuantity })
                });
                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || 'Lỗi khi cập nhật số lượng');
                }
                await response.json();
                return true;
            } catch (error) {
                console.error("Lỗi khi cập nhật:", error);
                alert(`Cập nhật thất bại: ${error.message}`);
                return false;
            }
        }

        async function handleAddNewPart(event) {
            event.preventDefault();
            if (!addPartForm) return;
            const formData = new FormData(addPartForm);
            const partData = Object.fromEntries(formData.entries());
            if (partErrorMessage) partErrorMessage.style.display = 'none';
            console.log("Đang gọi API: POST /api/v1/parts", partData);
            try {
                const response = await fetch(`${API_BASE_URL}/parts`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(partData)
                });
                if (!response.ok) {
                    const errorData = await response.json();
                    let errorMsg = errorData.message || 'Lỗi không xác định';
                    if (errorData.details) errorMsg = Object.values(errorData.details).join(', ');
                    throw new Error(errorMsg);
                }
                const newPart = await response.json();
                console.log("Thêm Part thành công:", newPart);
                if (addPartModal) addPartModal.classList.remove('show');
                if (addPartForm) addPartForm.reset();
                alert(`Thêm Part "${newPart.name}" (ID: ${newPart.partId}) thành công!\n\nBây giờ, hãy dùng nút "Add Stock" để nhập kho cho Part này.`);
            } catch (error) {
                console.error("Lỗi khi thêm Part:", error);
                if (partErrorMessage) {
                    partErrorMessage.textContent = `Lỗi: ${error.message}`;
                    partErrorMessage.style.display = 'block';
                }
            }
        }

        async function handleAddStock(event) {
            event.preventDefault();
            if (!addStockForm) return;
            const formData = new FormData(addStockForm);
            const stockData = Object.fromEntries(formData.entries());
            stockData.partId = parseInt(stockData.partId);
            stockData.quantity = parseInt(stockData.quantity);
            if (stockErrorMessage) stockErrorMessage.style.display = 'none';
            console.log("Đang gọi API: POST /api/v1/inventory/stock", stockData);
            try {
                const response = await fetch(`${API_BASE_URL}/inventory/stock`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(stockData)
                });
                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || 'Lỗi không xác định');
                }
                await response.json();
                console.log("Nhập kho thành công");
                if (addStockModal) addStockModal.classList.remove('show');
                if (addStockForm) addStockForm.reset();
                loadAndDisplayData();
            } catch (error) {
                console.error("Lỗi khi nhập kho:", error);
                if (stockErrorMessage) {
                    stockErrorMessage.textContent = `Lỗi: ${error.message}`;
                    stockErrorMessage.style.display = 'block';
                }
            }
        }

        if (tableBody) {
            tableBody.addEventListener('click', async (e) => {
                const editButton = e.target.closest('.edit-btn');
                if (!editButton) return;
                const row = editButton.closest('tr');
                const input = row.querySelector('.quantity-input');
                const icon = editButton.querySelector('i');
                const inventoryId = row.getAttribute('data-id');
                if (input.hasAttribute('readonly')) {
                    input.removeAttribute('readonly');
                    input.focus();
                    input.select();
                    icon.classList.remove('fa-pencil');
                    icon.classList.add('fa-check');
                    editButton.title = "Lưu thay đổi";
                } else {
                    const newQuantity = input.value;
                    const success = await updateQuantity(inventoryId, newQuantity);
                    if (success) {
                        input.setAttribute('readonly', true);
                        icon.classList.remove('fa-check');
                        icon.classList.add('fa-pencil');
                        editButton.title = "Chỉnh sửa số lượng";
                    } else {
                        loadAndDisplayData();
                    }
                }
            });
        }

        if (addPartBtn) addPartBtn.addEventListener('click', () => { if (partErrorMessage) partErrorMessage.style.display = 'none'; if (addPartModal) addPartModal.classList.add('show'); });
        if (closePartModalBtn) closePartModalBtn.addEventListener('click', () => { if (addPartModal) addPartModal.classList.remove('show'); });
        if (cancelPartModalBtn) cancelPartModalBtn.addEventListener('click', () => { if (addPartModal) addPartModal.classList.remove('show'); });
        if (addStockBtn) addStockBtn.addEventListener('click', () => { if (stockErrorMessage) stockErrorMessage.style.display = 'none'; if (addStockModal) addStockModal.classList.add('show'); });
        if (closeStockModalBtn) closeStockModalBtn.addEventListener('click', () => { if (addStockModal) addStockModal.classList.remove('show'); });
        if (cancelStockModalBtn) cancelStockModalBtn.addEventListener('click', () => { if (addStockModal) addStockModal.classList.remove('show'); });
        if (addPartForm) addPartForm.addEventListener('submit', handleAddNewPart);
        if (addStockForm) addStockForm.addEventListener('submit', handleAddStock);

        window.addEventListener('click', (e) => {
            if (e.target === addPartModal) addPartModal.classList.remove('show');
            if (e.target === addStockModal) addStockModal.classList.remove('show');
        });

        async function loadAndDisplayData() {
            const data = await fetchInventoryData();
            populateTable(data);
        }

        loadAndDisplayData();
    }

    // ================= REQUEST PAGE =================
    if (currentPage === 'request.html') {
        const tableBody = document.getElementById('request-table-body');
        const reloadBtn = document.getElementById('reload-data-btn');

        let MOCK_DATA = [
            { requestId: 2025001, fullName: 'Lam Bao Nghi', username: 'lam.nghi@oem.com', createdBy: 'nguyen.hung',
                proposedRoles: [{ name: 'ROLE_SC_STAFF' }], createdAt: new Date('2025-10-25T10:30:00'), status: 'PENDING' },
            { requestId: 2025002, fullName: 'Nguyễn Ngọc Anh', username: 'ngoc.anh@oem.com', createdBy: 'tran.mai',
                proposedRoles: [{ name: 'ROLE_EVM_STAFF' }], createdAt: new Date('2025-10-20T15:45:00'), status: 'APPROVED' },
            { requestId: 2025003, fullName: 'Lê Tấn Hùng', username: 'tan.hung@oem.com', createdBy: 'phan.quang',
                proposedRoles: [{ name: 'ROLE_ADMIN' }, { name: 'ROLE_MANAGER' }], createdAt: new Date('2025-10-15T08:00:00'), status: 'REJECTED' }
        ];

        const openModal = (requestId, type) => {
            const modal = document.createElement('div');
            modal.className = 'modal-overlay';
            modal.innerHTML = `
                <div class="modal">
                    <h3 style="color:${type === 'approve' ? '#27ae60' : '#e74c3c'}">
                        <i class="fa-solid ${type === 'approve' ? 'fa-check-circle' : 'fa-xmark-circle'}"></i>
                        ${type === 'approve' ? 'Duyệt Phiếu' : 'Từ Chối Phiếu'}
                    </h3>
                    <p>Bạn có chắc chắn muốn ${type === 'approve' ? 'duyệt' : 'từ chối'} phiếu <b>SR-${requestId}</b> này không?</p>
                    ${type === 'reject' ? '<textarea id="reason" placeholder="Nhập lý do từ chối..."></textarea>' : ''}
                    <div class="modal-actions">
                        <button class="btn btn-cancel">Hủy</button>
                        <button class="btn ${type === 'approve' ? 'btn-approve' : 'btn-reject'}">
                            ${type === 'approve' ? 'Xác nhận Duyệt' : 'Xác nhận Từ chối'}
                        </button>
                    </div>
                </div>
            `;
            document.body.appendChild(modal);
            modal.querySelector('.btn-cancel').onclick = () => modal.remove();
            modal.querySelector(`.${type === 'approve' ? 'btn-approve' : 'btn-reject'}`).onclick = () => {
                handleAction(requestId, type);
                modal.remove();
            };
        };

        const handleAction = (requestId, action) => {
            const index = MOCK_DATA.findIndex(r => r.requestId === requestId);
            if (index !== -1) {
                MOCK_DATA[index].status = action === 'approve' ? 'APPROVED' : 'REJECTED';
                renderRequests(MOCK_DATA);
                alert(`Phiếu ${requestId} đã được ${action === 'approve' ? 'DUYỆT' : 'TỪ CHỐI'}.`);
            }
        };

        const createRoleHtml = roles => roles.map(r => createBadge(r.name, 'role')).join(' ');

        const createRow = r => {
            const row = document.createElement('tr');
            const date = new Date(r.createdAt);
            const formatted = date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
            const processed = ['APPROVED', 'REJECTED'].includes(r.status);
            const roleHtml = createRoleHtml(r.proposedRoles);

            let actions = processed
                ? `<button class="badge-action badge-view"><i class="fa-solid fa-eye"></i> Xem</button>`
                : `<button class="badge-action badge-approve"><i class="fa-solid fa-check"></i> Duyệt</button>
                   <button class="badge-action badge-reject"><i class="fa-solid fa-times"></i> Từ chối</button>`;

            row.innerHTML = `
                <td>SR-${r.requestId}</td>
                <td>${r.fullName}</td>
                <td>${r.username}</td>
                <td>${roleHtml}</td>
                <td>${r.createdBy}</td>
                <td>${formatted}</td>
                <td>${createBadge(r.status)}</td>
                <td>${actions}</td>
            `;
            if (!processed) {
                row.querySelector('.badge-approve').onclick = () => openModal(r.requestId, 'approve');
                row.querySelector('.badge-reject').onclick = () => openModal(r.requestId, 'reject');
            }
            return row;
        };

        const renderRequests = data => {
            if (!tableBody) return;
            tableBody.innerHTML = '';
            if (data.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Không có dữ liệu</td></tr>';
                return;
            }
            data.forEach(r => tableBody.appendChild(createRow(r)));
        };

        renderRequests(MOCK_DATA);
        if (reloadBtn) reloadBtn.onclick = () => { renderRequests(MOCK_DATA); alert('Dữ liệu đã tải lại!'); };
    }

    // ================= USERS PAGE =================4

    if (isUsersPage) {
        const tableBody = document.getElementById('user-table-body');
        const modal = document.getElementById('user-modal');
        const modalTitle = document.getElementById('modal-title');
        const saveBtn = document.getElementById('save-user-btn');
        const closeModalBtn = document.getElementById('close-modal-btn');
        const togglePassIcon = document.getElementById('toggle-password');
        const passwordInput = document.getElementById('password');
        const searchInput = document.getElementById('search-input');
        const addBtn = document.getElementById('add-user-btn');

        let MOCK_USERS = [
            { userId: 1, username: 'admin', password: '123456', email: 'admin@test.com', phone: '0123456789', roles: [{ name: 'ROLE_ADMIN' }], serviceCenterId: 1, status: 'ACTIVE' },
            { userId: 2, username: 'tech1', password: 'abc123', email: 'tech1@test.com', phone: '0987654321', roles: [{ name: 'ROLE_TECHNICIAN' }], serviceCenterId: 2, status: 'ACTIVE' },
            { userId: 3, username: 'userA', password: 'user123', email: 'userA@test.com', phone: '0333444555', roles: [{ name: 'ROLE_USER' }], serviceCenterId: 2, status: 'INACTIVE' }
        ];

        let filteredUsers = [...MOCK_USERS];

        const deleteUser = (userId, username) => {
            Swal.fire({
                title: `Xác nhận XÓA người dùng ${username}?`,
                text: "Thao tác này không thể hoàn tác!",
                icon: "warning",
                showCancelButton: true,
                confirmButtonColor: "#d33",
                cancelButtonColor: "#3085d6",
                confirmButtonText: "Vâng, Xóa!",
                cancelButtonText: "Hủy"
            }).then((result) => {
                if (result.isConfirmed) {
                    MOCK_USERS = MOCK_USERS.filter(u => u.userId !== userId);
                    filteredUsers = [...MOCK_USERS];
                    renderTable();
                    Swal.fire('Đã Xóa!', `Người dùng ${username} đã bị xóa.`, 'success');
                }
            });
        };

        const renderTable = () => {
            if (!tableBody) return;
            tableBody.innerHTML = '';
            if (filteredUsers.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="9" style="text-align:center;">Không tìm thấy người dùng.</td></tr>';
                return;
            }
            filteredUsers.forEach(user => {
                const row = document.createElement('tr');
                const maskedPassword = user.password ? '*'.repeat(user.password.length) : '';
                const roleName = user.roles[0].name;
                row.innerHTML = `
                    <td>${user.userId}</td>
                    <td>${user.username}</td>
                    <td>
                        <span class="masked-password">${maskedPassword}</span>
                        <i class="fa-solid fa-eye toggle-pass" data-id="${user.userId}" style="margin-left:5px; cursor:pointer;"></i>
                    </td>
                    <td>${user.email}</td>
                    <td>${user.phone}</td>
                    <td>${createBadge(roleName, 'role')}</td>
                    <td>${user.serviceCenterId || '-'}</td>
                    <td>${createBadge(user.status, 'status')}</td>
                    <td>
                        <button class="btn-icon btn-edit" data-id="${user.userId}"><i class="fa-solid fa-pen"></i></button>
                        <button class="btn-icon btn-delete" data-id="${user.userId}"><i class="fa-solid fa-trash"></i></button>
                    </td>`;
                tableBody.appendChild(row);

                row.querySelector('.toggle-pass').addEventListener('click', e => {
                    const targetUser = MOCK_USERS.find(u => u.userId == e.target.dataset.id);
                    const spanPass = e.target.previousElementSibling;
                    if (spanPass.textContent.includes('*')) {
                        spanPass.textContent = targetUser.password;
                        e.target.classList.replace('fa-eye', 'fa-eye-slash');
                    } else {
                        spanPass.textContent = '*'.repeat(targetUser.password.length);
                        e.target.classList.replace('fa-eye-slash', 'fa-eye');
                    }
                });

                row.querySelector('.btn-edit').addEventListener('click', e => {
                    const targetUser = MOCK_USERS.find(u => u.userId == e.currentTarget.dataset.id);
                    if (!modal || !modalTitle) return;
                    modalTitle.textContent = 'Chỉnh sửa Người dùng';
                    modal.style.display = 'flex';
                    document.getElementById('user-id').value = targetUser.userId;
                    document.getElementById('username').value = targetUser.username;
                    document.getElementById('password').value = targetUser.password;
                    document.getElementById('email').value = targetUser.email;
                    document.getElementById('phone').value = targetUser.phone;
                    document.getElementById('role').value = targetUser.roles[0].name;
                    document.getElementById('status').value = targetUser.status;
                });

                row.querySelector('.btn-delete').addEventListener('click', e => {
                    const targetUser = MOCK_USERS.find(u => u.userId == e.currentTarget.dataset.id);
                    deleteUser(targetUser.userId, targetUser.username);
                });
            });
        };

        if (searchInput) {
            searchInput.addEventListener('input', () => {
                const keyword = searchInput.value.toLowerCase();
                filteredUsers = MOCK_USERS.filter(u => u.username.toLowerCase().includes(keyword) || u.email.toLowerCase().includes(keyword));
                renderTable();
            });
        }

        if (addBtn) {
            addBtn.addEventListener('click', () => {
                if (!modal || !modalTitle) return;
                modalTitle.textContent = 'Thêm Người dùng Mới';
                modal.style.display = 'flex';
                document.getElementById('user-id').value = '';
                document.getElementById('username').value = '';
                document.getElementById('password').value = '';
                document.getElementById('email').value = '';
                document.getElementById('phone').value = '';
                document.getElementById('role').value = 'ROLE_USER';
                document.getElementById('status').value = 'ACTIVE';
            });
        }

        if (togglePassIcon && passwordInput) {
            togglePassIcon.addEventListener('click', () => {
                if (passwordInput.type === 'password') {
                    passwordInput.type = 'text';
                    togglePassIcon.classList.replace('fa-eye', 'fa-eye-slash');
                } else {
                    passwordInput.type = 'password';
                    togglePassIcon.classList.replace('fa-eye-slash', 'fa-eye');
                }
            });
        }

        if (closeModalBtn) {
            closeModalBtn.addEventListener('click', () => { if (modal) modal.style.display = 'none'; });
            window.onclick = e => { if (e.target === modal) modal.style.display = 'none'; };
        }

        if (saveBtn) {
            saveBtn.addEventListener('click', () => {
                const id = document.getElementById('user-id').value;
                const userData = {
                    userId: id ? parseInt(id) : Date.now(),
                    username: document.getElementById('username').value,
                    password: document.getElementById('password').value,
                    email: document.getElementById('email').value,
                    phone: document.getElementById('phone').value,
                    roles: [{ name: document.getElementById('role').value }],
                    serviceCenterId: 1,
                    status: document.getElementById('status').value
                };
                if (!userData.username || !userData.password || !userData.email) {
                    Swal.fire('Lỗi!', 'Vui lòng điền đủ Username, Password và Email.', 'error');
                    return;
                }
                if (id) {
                    MOCK_USERS = MOCK_USERS.map(u => u.userId == userData.userId ? userData : u);
                } else {
                    MOCK_USERS.push(userData);
                }
                filteredUsers = [...MOCK_USERS];
                renderTable();
                if (modal) modal.style.display = 'none';
                Swal.fire('Thành công!', `Người dùng ${userData.username} đã được ${id ? 'cập nhật' : 'thêm mới'}.`, 'success');
            });
        }

        renderTable();
    }
});