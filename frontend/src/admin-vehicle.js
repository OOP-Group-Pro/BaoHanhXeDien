import { checkAuth } from './utils/auth.js';
import { renderAdminSidebar } from './components/AdminSidebar.js';
import { api } from './services/apiClient.js';

// --- API ---
const VehicleService = {
    getAll: () => api.get('/vehicles'),
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
    tbody.innerHTML = '<tr><td colspan="7" class="text-center"><div class="spinner-border spinner-border-sm"></div></td></tr>';

    try {
        const data = await VehicleService.getAll(); // Giả sử trả về List
        renderTable(data);
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-danger text-center">${e.message}</td></tr>`;
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
        const partsSummary = (v.installedParts || [])
            .map(p => `<span class="badge bg-light text-dark border">${p.partType}: ${p.serialNumber}</span>`)
            .join(' ');

        tbody.innerHTML += `
            <tr>
                <td class="fw-bold text-primary">${v.vin}</td>
                <td>${v.licensePlate}</td>
                <td>${v.model}</td>
                <td>${v.customerName || 'Khách lẻ'}</td>
                <td>${new Date(v.warrantyStartDate).toLocaleDateString('vi-VN')}</td>
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
    const btnAdd = document.getElementById('btn-add-vehicle');
    const btnClose = document.getElementById('close-vehicle-modal');
    const btnAddPartRow = document.getElementById('btn-add-part-row');
    const form = document.getElementById('vehicle-form');

    btnAdd.addEventListener('click', () => {
        form.reset();
        document.getElementById('installed-parts-tbody').innerHTML = ''; // Xóa các dòng cũ
        // Thêm sẵn 1 dòng PIN và MOTOR cho tiện
        addPartRow('PIN', '', '');
        addPartRow('MOTOR', '', '');
        modal.classList.add('show');
    });

    btnClose.addEventListener('click', () => modal.classList.remove('show'));

    btnAddPartRow.addEventListener('click', () => {
        addPartRow('', '', '');
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const vin = document.getElementById('inp-vin').value.toUpperCase();

        // Gom dữ liệu từ bảng phụ tùng
        const installedParts = [];
        document.querySelectorAll('.part-row').forEach(row => {
            const type = row.querySelector('.inp-part-type').value;
            const serial = row.querySelector('.inp-part-serial').value;
            const name = row.querySelector('.inp-part-name').value;

            if(type && serial) {
                installedParts.push({ partType: type, serialNumber: serial, partName: name });
            }
        });

        const payload = {
            vin: vin,
            licensePlate: document.getElementById('inp-plate').value.toUpperCase(),
            model: document.getElementById('inp-model').value,
            warrantyStartDate: document.getElementById('inp-warranty-date').value,
            installedParts: installedParts // Gửi list này xuống Backend
        };

        try {
            await VehicleService.create(payload);
            alert("✅ Đăng ký xe thành công!");
            modal.classList.remove('show');
            loadVehicles();
        } catch (err) {
            alert("Lỗi: " + err.message);
        }
    });
}

function addPartRow(type, name, serial) {
    const tbody = document.getElementById('installed-parts-tbody');
    const tr = document.createElement('tr');
    tr.className = 'part-row';
    tr.innerHTML = `
        <td style="padding: 5px;">
            <select class="inp-part-type" style="width:100%; padding:6px; border:1px solid #ddd; border-radius:4px;">
                <option value="PIN" ${type==='PIN'?'selected':''}>PIN (Battery)</option>
                <option value="MOTOR" ${type==='MOTOR'?'selected':''}>MOTOR</option>
                <option value="BMS" ${type==='BMS'?'selected':''}>BMS</option>
                <option value="INVERTER" ${type==='INVERTER'?'selected':''}>INVERTER</option>
                <option value="CHARGER" ${type==='CHARGER'?'selected':''}>CHARGER</option>
            </select>
        </td>
        <td style="padding: 5px;"><input type="text" class="inp-part-name" value="${name}" placeholder="VD: Pin LFP 50kW" style="width:100%; padding:6px; border:1px solid #ddd; border-radius:4px;"></td>
        <td style="padding: 5px;"><input type="text" class="inp-part-serial" value="${serial}" placeholder="SN-..." style="width:100%; padding:6px; border:1px solid #ddd; border-radius:4px;"></td>
        <td style="text-align:center;">
            <button type="button" class="btn-icon btn-delete" onclick="this.closest('tr').remove()"><i class="fa-solid fa-trash"></i></button>
        </td>
    `;
    tbody.appendChild(tr);
}