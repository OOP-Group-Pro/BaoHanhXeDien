import { checkAuth } from './utils/auth.js';
import { renderAdminSidebar } from './components/AdminSidebar.js';
import { api } from './services/apiClient.js';

// --- API ---
const VehicleService = {
    // Xử lý trường hợp API trả về Page hoặc List
    getAll: async () => {
        const res = await api.get('/vehicles?page=0&size=100'); // Lấy tạm 100 xe mới nhất
        return res.content || res; // Trả về mảng
    },
    create: (data) => api.post('/vehicles', data),
    update: (vin, data) => api.put(`/vehicles/${vin}`, data)
};

document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth('ROLE_ADMIN')) return;
    renderAdminSidebar();
    loadVehicles();
    setupEvents();
});

async function loadVehicles() {
    const tbody = document.getElementById('vehicle-tbody');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="7" class="text-center"><div class="spinner-border spinner-border-sm"></div> Đang tải...</td></tr>';

    try {
        // 1. Gọi API
        const response = await VehicleService.getAll();
        console.log("📦 Dữ liệu xe nhận được:", response); // Xem log để biết cấu trúc

        // 2. Xử lý dữ liệu (Fix lỗi forEach)
        let vehicles = [];

        if (Array.isArray(response)) {
            // Trường hợp 1: Trả về mảng trực tiếp [{}, {}]
            vehicles = response;
        } else if (response && Array.isArray(response.content)) {
            // Trường hợp 2: Trả về Page của Spring { content: [{}, {}], totalPages: ... }
            vehicles = response.content;
        } else if (response && Array.isArray(response.data)) {
            // Trường hợp 3: Trả về ApiResponse { status: "success", data: [{}, {}] }
            vehicles = response.data;
        } else if (response && response.data && Array.isArray(response.data.content)) {
            // Trường hợp 4: ApiResponse lồng Page { data: { content: [] } }
            vehicles = response.data.content;
        } else {
            // Trường hợp rỗng hoặc lỗi cấu trúc
            vehicles = [];
        }

        // 3. Render
        renderTable(vehicles);

    } catch (e) {
        console.error(e);
        tbody.innerHTML = `<tr><td colspan="7" class="text-danger text-center">Lỗi: ${e.message}</td></tr>`;
    }
}

function renderTable(vehicles) {
    const tbody = document.getElementById('vehicle-tbody');
    tbody.innerHTML = '';

    if (!vehicles || vehicles.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Chưa có xe nào.</td></tr>';
        return;
    }

    vehicles.forEach(v => {
        // Hiển thị tóm tắt các phụ tùng chính
        // Backend trả về list tên là 'parts' hoặc 'installedParts', kiểm tra cả 2
        const partsList = v.installedParts || v.parts || [];

        const partsSummary = partsList
            .map(p => `<span class="badge bg-light text-dark border" style="font-size:11px; margin-right:2px">${p.partType}: ${p.serialNumber}</span>`)
            .join(' ');

        // 2. SỬA TÊN TRƯỜNG HIỂN THỊ (v.vehicleVin thay vì v.vin)
        tbody.innerHTML += `
            <tr>
                <td class="fw-bold text-primary font-monospace">${v.vehicleVin}</td>
                <td>${v.model}</td>
                <td><span class="fw-bold">${v.licensePlate || '--'}</span></td>
                <td>${v.warrantyStartDate ? new Date(v.warrantyStartDate).toLocaleDateString('vi-VN') : '-'}</td>
                <td>${(v.currentOdometer || 0).toLocaleString()} Km</td>
                <td><div style="display:flex; flex-wrap:wrap; gap:4px;">${partsSummary}</div></td>
                <td>
                    <button class="btn-icon btn-edit" onclick="alert('Chức năng sửa đang phát triển')"><i class="fa-solid fa-pen"></i></button>
                </td>
            </tr>
        `;
    });
}

function setupEvents() {
    const modal = document.getElementById('vehicle-modal');
    const btnOpen = document.getElementById('btn-open-create-modal'); // Sửa ID nút mở modal cho khớp HTML
    const btnClose = document.getElementById('close-vehicle-modal');
    const btnAddPartRow = document.getElementById('btn-add-part-row');
    const form = document.getElementById('vehicle-form');
    const btnRefresh = document.getElementById('btn-refresh');

    // Mở Modal
    if (btnOpen) {
        btnOpen.addEventListener('click', () => {
            form.reset();
            // 3. SỬA ID BODY BẢNG PHỤ TÙNG (Cho khớp HTML)
            const partBody = document.getElementById('part-rows-body');
            if(partBody) {
                partBody.innerHTML = '';
                // Thêm sẵn 1 dòng PIN và MOTOR cho tiện
                addPartRow('PIN', '', '');
                addPartRow('MOTOR', '', '');
            }
            modal.classList.add('show');
        });
    }

    if (btnClose) btnClose.addEventListener('click', () => modal.classList.remove('show'));
    if (btnRefresh) btnRefresh.addEventListener('click', loadVehicles);

    if (btnAddPartRow) {
        btnAddPartRow.addEventListener('click', () => {
            addPartRow('', '', '');
        });
    }

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const vin = document.getElementById('inp-vin').value.trim().toUpperCase();

            if (vin.length !== 17) {
                alert("Số VIN phải đúng 17 ký tự!");
                return;
            }

            // Gom dữ liệu từ bảng phụ tùng
            const installedParts = [];
            document.querySelectorAll('.part-row').forEach(row => {
                const type = row.querySelector('.inp-type').value;   // Sửa class selector
                const serial = row.querySelector('.inp-serial').value.trim(); // Sửa class selector

                // Không cần partName input nếu không có, hoặc thêm vào nếu cần

                if(type && serial) {
                    installedParts.push({
                        partType: type,
                        serialNumber: serial,
                        installDate: new Date().toISOString(),
                        status: 'INSTALLED'
                    });
                }
            });

            // 4. SỬA TÊN TRƯỜNG GỬI LÊN (Khớp VehicleRequestDTO)
            const payload = {
                vehicleVin: vin, // Backend cần vehicleVin
                licensePlate: document.getElementById('inp-plate').value.trim().toUpperCase(),
                model: document.getElementById('inp-model').value,
                warrantyStartDate: document.getElementById('inp-date').value,
                currentOdometer: document.getElementById('inp-odo').value || 0,
                installedParts: installedParts
            };

            console.log("📦 Payload gửi đi:", payload);

            try {
                const btnSave = document.getElementById('btn-save-vehicle'); // Giả sử ID nút submit là này
                if(btnSave) {
                    btnSave.textContent = "Đang lưu...";
                    btnSave.disabled = true;
                }

                await VehicleService.create(payload);

                alert("✅ Đăng ký xe thành công!");
                modal.classList.remove('show');
                loadVehicles();
            } catch (err) {
                console.error(err);
                alert("Lỗi: " + err.message);
            } finally {
                const btnSave = document.querySelector('#vehicle-form button[type="submit"]');
                if(btnSave) {
                    btnSave.textContent = "Lưu Hồ Sơ";
                    btnSave.disabled = false;
                }
            }
        });
    }
}

function addPartRow(type = '', name = '', serial = '') {
    // 3. SỬA ID BODY (Khớp HTML)
    const tbody = document.getElementById('part-rows-body');
    if (!tbody) return;

    const tr = document.createElement('tr');
    tr.className = 'part-row';

    const options = [
        {val: 'PIN', label: 'PIN (Battery)'},
        {val: 'MOTOR', label: 'MOTOR'},
        {val: 'BMS', label: 'BMS'},
        {val: 'INVERTER', label: 'INVERTER'},
        {val: 'CHARGER', label: 'CHARGER'}
    ].map(o => `<option value="${o.val}" ${o.val === type ? 'selected' : ''}>${o.label}</option>`).join('');

    // Sửa class input cho khớp với querySelector ở trên (.inp-type, .inp-serial)
    tr.innerHTML = `
        <td style="padding: 5px;">
            <select class="inp-type" style="width:100%; padding:6px; border:1px solid #ddd; border-radius:4px;">
                <option value="">-- Chọn --</option>
                ${options}
            </select>
        </td>
        <td style="padding: 5px;">
            <input type="text" class="inp-serial" value="${serial}" placeholder="SN-..." style="width:100%; padding:6px; border:1px solid #ddd; border-radius:4px;">
        </td>
        <td style="text-align:center;">
            <button type="button" class="btn-icon btn-delete" style="color:red" onclick="this.closest('tr').remove()"><i class="fa-solid fa-trash"></i></button>
        </td>
    `;
    tbody.appendChild(tr);
}