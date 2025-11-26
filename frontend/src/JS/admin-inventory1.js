import { api } from '../services/apiClient.js';
import { logout } from '../services/authService.js';

document.addEventListener('DOMContentLoaded', () => {

    // DOM Elements
    const tableBody = document.getElementById('inventory-table-body');
    const searchInput = document.getElementById('search-input');

    // Modal Elements
    const addPartModal = document.getElementById('add-part-modal');
    const addPartBtn = document.getElementById('add-new-part-btn');
    const closePartModalBtn = document.getElementById('close-part-modal');
    const cancelPartModalBtn = document.getElementById('cancel-part-modal');
    const addPartForm = document.getElementById('add-part-form');
    const partErrorMessage = document.getElementById('part-error-message');
    const modalTitle = document.querySelector('#add-part-modal h2'); // Lấy tiêu đề modal để đổi tên

    // Biến toàn cục chứa dữ liệu gốc để tìm kiếm
    let allPartsData = [];

    // ================= 1. LOAD DATA & RENDER =================
    async function loadPartsList() {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:40px;"><div class="spinner-border text-primary"></div> Đang tải...</td></tr>`;
        try {
            const response = await api.get('/parts');
            allPartsData = Array.isArray(response) ? response : (response.content || []);

            renderTable(allPartsData);
            updateStats(allPartsData);

        } catch (err) {
            console.error(err);
            tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:red;padding:40px;">Lỗi kết nối: ${err.message}</td></tr>`;
        }
    }

    function renderTable(data) {
        if (!data || data.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:40px;color:#666;">Không tìm thấy dữ liệu phù hợp.</td></tr>`;
            return;
        }
        tableBody.innerHTML = '';

        data.forEach(item => {
            const priceFormatted = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.price || 0);

            // Tạo badge class dựa trên partType
            const typeClass = `badge-${item.partType || 'OTHER'}`;

            const row = document.createElement('tr');
            row.innerHTML = `
                <td><span style="color:#888; font-size:0.9em">#${item.partId}</span></td>
                <td>
                    <div style="display:flex; flex-direction:column;">
                        <span style="font-weight:600; color:#1f2937; font-size:1.05em">${item.name}</span>
                        <small style="color:#6b7280; font-family:monospace;">SKU: ${item.serialNumber || 'N/A'}</small>
                    </div>
                </td>
                <td>${item.manufacturer || '--'}</td>
                <td><span class="badge-type ${typeClass}">${item.partType || 'Khác'}</span></td>
                <td style="font-weight:600; color:#28a745;">${priceFormatted}</td>
                <td>
                    <span style="font-weight:bold; font-size:1.1em; ${item.inventoryQuantity > 0 ? 'color:#1f2937' : 'color:#dc3545'}">
                        ${item.inventoryQuantity || 0}
                    </span>
                </td>
                <td>
                    <button class="btn-action btn-edit" data-id="${item.partId}" title="Chỉnh sửa">
                        <i class="fa-solid fa-pen-to-square"></i>
                    </button>
                    <button class="btn-action btn-delete" data-id="${item.partId}" title="Xóa">
                        <i class="fa-solid fa-trash"></i>
                    </button>
                </td>
            `;
            tableBody.appendChild(row);
        });

        // Gán sự kiện click cho các nút trong bảng (Edit/Delete)
        attachRowEvents();
    }

    function attachRowEvents() {
        // Nút Sửa
        document.querySelectorAll('.btn-edit').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = btn.getAttribute('data-id');
                openEditModal(id);
            });
        });

        // Nút Xóa
        document.querySelectorAll('.btn-delete').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = btn.getAttribute('data-id');
                handleDelete(id);
            });
        });
    }

    function updateStats(data) {
        const setText = (id, val) => {
            const el = document.getElementById(id);
            if(el) el.textContent = val;
        };

        setText('total-parts', data.length);
        setText('battery-count', data.filter(i => i.partType === 'BATTERY').length);
        setText('motor-count', data.filter(i => i.partType === 'MOTOR').length);
        setText('tire-count', data.filter(i => i.partType === 'TIRE').length);
        setText('electronic-count', data.filter(i => i.partType === 'ELECTRONIC').length);
        setText('another-count', data.filter(i => i.partType === 'ANOTHER').length);
    }

    // ================= 2. TÌM KIẾM (SEARCH) =================
    searchInput?.addEventListener('input', (e) => {
        const keyword = e.target.value.toLowerCase().trim();

        // Lọc trên dữ liệu local (allPartsData)
        const filteredData = allPartsData.filter(item =>
            (item.name && item.name.toLowerCase().includes(keyword)) ||
            (item.serialNumber && item.serialNumber.toLowerCase().includes(keyword)) ||
            (item.manufacturer && item.manufacturer.toLowerCase().includes(keyword))
        );

        renderTable(filteredData);
    });

    // ================= 3. XÓA (DELETE) =================
    async function handleDelete(id) {
        // Tìm tên phụ tùng để hiển thị trong thông báo cho rõ
        const part = allPartsData.find(p => p.partId == id);
        const name = part ? part.name : 'phụ tùng này';

        if(confirm(`⚠️ CẢNH BÁO: Bạn có chắc chắn muốn xóa "${name}" không?\nHành động này không thể hoàn tác!`)) {
            try {
                await api.delete(`/parts/${id}`);
                alert('Đã xóa thành công!');
                loadPartsList(); // Tải lại danh sách
            } catch (err) {
                alert('Lỗi khi xóa: ' + (err.message || 'Không xác định'));
            }
        }
    }

    // ================= 4. THÊM & SỬA (MODAL) =================

    // Hàm reset form về trạng thái "Thêm mới"
    const resetModal = () => {
        addPartForm.reset();
        document.getElementById('part-id-hidden').value = ''; // Xóa ID
        modalTitle.innerHTML = '<i class="fa-solid fa-box"></i> Định nghĩa Phụ Tùng Mới';
        if (partErrorMessage) partErrorMessage.style.display = 'none';
    };

    // Mở Modal Thêm
    addPartBtn?.addEventListener('click', () => {
        resetModal();
        addPartModal.classList.add('show');
    });

    // Mở Modal Sửa (Fill dữ liệu cũ)
    function openEditModal(id) {
        const part = allPartsData.find(p => p.partId == id);
        if (!part) return;

        // Điền dữ liệu vào form
        resetModal();

        // Đổi tiêu đề
        modalTitle.innerHTML = `<i class="fa-solid fa-pen"></i> Cập nhật: ${part.name}`;

        // Fill value input
        const form = addPartForm;
        form.elements['partId'].value = part.partId;
        form.elements['name'].value = part.name;
        form.elements['serialNumber'].value = part.serialNumber;
        form.elements['manufacturer'].value = part.manufacturer;
        form.elements['partType'].value = part.partType;
        form.elements['price'].value = part.price;
        if (part.warrantyPolicyId) form.elements['warrantyPolicyId'].value = part.warrantyPolicyId;

        addPartModal.classList.add('show');
    }

    // Đóng Modal
    const closeModal = () => addPartModal.classList.remove('show');
    if (closePartModalBtn) closePartModalBtn.addEventListener('click', closeModal);
    if (cancelPartModalBtn) cancelPartModalBtn.addEventListener('click', closeModal);
    window.addEventListener('click', e => { if (e.target === addPartModal) closeModal(); });

    // Xử lý Submit Form (Chung cho cả Thêm và Sửa)
    if (addPartForm) {
        addPartForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(addPartForm);
            const id = formData.get('partId'); // Lấy ID từ input ẩn

            const partData = {
                name: formData.get('name'),
                serialNumber: formData.get('serialNumber'),
                manufacturer: formData.get('manufacturer'),
                partType: formData.get('partType'),
                price: parseFloat(formData.get('price')) || 0,
                warrantyPolicyId: formData.get('warrantyPolicyId') ? parseInt(formData.get('warrantyPolicyId')) : null
            };

            try {
                if (id) {
                    // --- LOGIC SỬA (PUT) ---
                    await api.put(`/parts/${id}`, partData);
                    alert('✅ Cập nhật thành công!');
                } else {
                    // --- LOGIC THÊM (POST) ---
                    await api.post('/parts', partData);
                    alert('✅ Thêm mới thành công!');
                }

                closeModal();
                loadPartsList(); // Reload lại dữ liệu
            } catch (error) {
                console.error(error);
                if (partErrorMessage) {
                    partErrorMessage.textContent = `❌ Lỗi: ${error.message || 'Lỗi hệ thống'}`;
                    partErrorMessage.style.display = 'block';
                }
            }
        });
    }

    // ================= LOGOUT =================
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if(confirm("Bạn có chắc chắn muốn đăng xuất?")) {
                logout();
            }
        });
    }

    // ================= INIT =================
    loadPartsList();

    // Tự động mở modal nếu có tham số URL (từ Sidebar)
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('openCreate') === 'true') {
        window.history.replaceState({}, document.title, window.location.pathname);
        if (addPartBtn) setTimeout(() => addPartBtn.click(), 200);
    }
});