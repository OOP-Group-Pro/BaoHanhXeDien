import { checkAuth } from '../utils/auth.js';
import { renderAdminSidebar } from '../components/AdminSidebar.js';
import { api } from '../services/apiClient.js';
import { getAllInventory, getParts } from '../services/partService.js'; // Import từ file service của bạn
import { getAllocationStatusForClaim } from '../services/partService.js'; // Hoặc service tương ứng

// --- API SERVICE ---
const InventoryService = {
    getAll: (page = 0, size = 10) => api.get(`/inventory/all-stock?page=${page}&size=${size}`),
    addStock: (data) => api.post('/inventory/stock', data),
    getAllAllocations: (page = 0, size = 10) => api.get(`/allocations?page=${page}&size=${size}`)
};

let currentPage = 0;
let allPartsList = []; // Cache danh sách phụ tùng để tìm kiếm trong Modal
let allInventoryData = []; // Khai báo biến toàn cục ở đầu file

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Init
    if (!checkAuth('ROLE_ADMIN')) return;
    renderAdminSidebar();

    // 2. Load dữ liệu
    loadInventoryTable();
    loadPartsForDropdown(); // Tải trước danh sách Part để nhét vào Modal

    // 3. Setup Event
    setupModalEvents();
    // Set up Tab inventory:
    setupTabs();
});

// --- LOAD BẢNG TỒN KHO ---
async function loadInventoryTable() {
    const tbody = document.getElementById('inventory-tbody');
    tbody.innerHTML = '<tr><td colspan="8" class="text-center py-5"><div class="spinner-border text-primary"></div></td></tr>';

    try {
        // Gọi API /inventory/all-stock
        // Lưu ý: Backend của bạn cần hỗ trợ Pageable
        const response = await api.get(`/inventory/all-stock?page=${currentPage}&size=10`);
        const data = response.content || [];

        allInventoryData = data; // 🔥 LƯU DỮ LIỆU GỐC VÀO BIẾN NÀY

        renderKPIs(data); // Tính toán KPI giả lập từ trang hiện tại (hoặc gọi API KPI riêng)
        renderTable(data);
        updatePagination(response);

    } catch (error) {
        console.error(error);
        tbody.innerHTML = `<tr><td colspan="8" class="text-center text-danger">Lỗi tải dữ liệu: ${error.message}</td></tr>`;
    }
}

function renderTable(items) {
    const tbody = document.getElementById('inventory-tbody');
    tbody.innerHTML = '';

    if (items.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-4">Kho trống.</td></tr>';
        return;
    }

    items.forEach(item => {
        // Format ngày tháng
        const date = item.updatedAt ? new Date(item.updatedAt).toLocaleDateString('vi-VN') : '-';

        // Badge status
        let badgeClass = 'badge-secondary';
        if(item.status === 'Available') badgeClass = 'bg-success text-white';
        if(item.status === 'Reserved') badgeClass = 'bg-warning text-dark';
        if(item.status === 'Defective') badgeClass = 'bg-danger text-white';

        tbody.innerHTML += `
            <tr>
                <td>#${item.inventoryId}</td>
                <td><img src="/assets/part-placeholder.png" class="rounded border" width="40" height="40" alt="Part"></td>
                <td>
                    <div class="fw-bold text-primary">${item.partName || 'Unknown'}</div>
                    <div class="d-flex gap-2 small text-muted">
                        <span>SKU: ${item.serialNumber}</span>
                        <span>Type: ${item.partType}</span>
                    </div>
                </td>
                <td><span class="badge bg-light text-dark border"><i class="fa-solid fa-warehouse"></i> ${item.location}</span></td>
                <td class="fw-bold fs-6">${item.quantity}</td>
                <td><span class="badge ${badgeClass}">${item.status}</span></td>
                <td class="small text-muted">${date}</td>
                <td>
                    <button class="btn-icon btn-edit" title="Điều chỉnh số lượng"><i class="fa-solid fa-pen-to-square"></i></button>
                </td>
            </tr>
        `;
    });
}

// --- LOAD DROPDOWN CHO MODAL ---
async function loadPartsForDropdown() {
    try {
        // Lấy 100 phụ tùng đầu tiên để demo (Thực tế nên dùng API Search Select2)
        const res = await getParts(0, 100);
        const parts = res.content || [];
        allPartsList = parts; // Lưu cache

        const select = document.getElementById('import-part-select');
        parts.forEach(p => {
            const option = document.createElement('option');
            option.value = p.partId;
            option.text = `${p.name} (SKU: ${p.serialNumber})`;
            select.appendChild(option);
        });

        // Kích hoạt Select2 (Thư viện tìm kiếm đẹp)
        $(select).select2({
            dropdownParent: $('#import-modal') // Fix lỗi không gõ được trong modal
        });

        // Sự kiện khi chọn Part -> Hiển thị Preview
        $(select).on('change', function() {
            const selectedId = $(this).val();
            const part = allPartsList.find(p => p.partId == selectedId);
            if(part) {
                document.getElementById('part-preview').classList.remove('d-none');
                document.getElementById('preview-name').textContent = part.partName;
                document.getElementById('preview-sku').textContent = part.serialNumber;
                document.getElementById('preview-type').textContent = part.partType;
            }
        });

    } catch (e) { console.warn("Lỗi tải danh sách part:", e); }
}

// --- MODAL EVENTS ---
function setupModalEvents() {
    const modal = document.getElementById('import-modal');

    document.getElementById('btn-import-stock').addEventListener('click', () => {
        modal.classList.add('show');
    });

    document.getElementById('close-import-modal').addEventListener('click', () => {
        modal.classList.remove('show');
    });

    // Logic nút Lưu Nhập kho
    const btnSave = document.getElementById('btn-save-import'); // Đảm bảo lấy đúng ID

    btnSave.addEventListener('click', async () => {
        // 1. Lấy giá trị từ giao diện
        // Lưu ý: Với Select2, đôi khi cần dùng jQuery để lấy value chuẩn nếu DOM chưa cập nhật
        // Nhưng thử DOM chuẩn trước:
        const partId = document.getElementById('import-part-select').value;
        const location = document.getElementById('import-location').value;
        const quantity = document.getElementById('import-quantity').value;

        // Log ra để kiểm tra xem lấy đúng chưa
        console.log("📦 Dữ liệu chuẩn bị gửi:", { partId, location, quantity });

        // 2. Validate
        if (!partId || !location || !quantity) {
            alert("Vui lòng chọn Phụ tùng, Vị trí và nhập Số lượng!");
            return;
        }

        // Hiệu ứng loading
        const originalText = btnSave.innerHTML;
        btnSave.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang xử lý...';
        btnSave.disabled = true;

        try {
            // 3. GỌI API (QUAN TRỌNG NHẤT)
            await InventoryService.addStock({
                partId: Number(partId),      // Chuyển thành số Long
                quantity: Number(quantity),  // Chuyển thành số Long
                location: String(location)   // Chuỗi mã trạm (VD: "101")
            });

            // 4. Nếu thành công
            alert("✅ Nhập kho thành công!");
            document.getElementById('import-modal').classList.remove('show');

            // Load lại bảng để thấy số lượng mới
            loadInventoryTable();

        } catch (error) {
            console.error("Lỗi nhập kho:", error);
            alert("❌ Lỗi: " + (error.message || "Không thể kết nối Server"));
        } finally {
            // Trả lại nút như cũ
            btnSave.innerHTML = originalText;
            btnSave.disabled = false;
        }
    });
}

function renderKPIs(data) {
    // data là danh sách PartInventoryResponse

    let totalQty = 0;
    let totalValue = 0;
    let lowStockCount = 0;
    let defectiveCount = 0;

    // Ngưỡng cảnh báo sắp hết hàng (Ví dụ: dưới 10 cái)
    const LOW_STOCK_THRESHOLD = 10;

    data.forEach(item => {
        // 1. Tổng số lượng
        const qty = item.quantity || 0;
        totalQty += qty;

        // 2. Tổng giá trị (Số lượng * Giá đơn vị)
        // Lưu ý: item.part có thể null nếu data lỗi, cần check an toàn
        const price = item.price ? (item.price || 0) : 0;
        totalValue += (qty * price);

        // 3. Đếm hàng sắp hết (Chỉ tính hàng tốt Available)
        if (item.status === 'Available' && qty < LOW_STOCK_THRESHOLD) {
            lowStockCount++;
        }

        // 4. Đếm hàng lỗi (Status là Defective)
        // Cần check đúng chính tả Enum trả về từ Backend (thường là viết hoa chữ đầu hoặc full hoa)
        // Code cũ bạn map là 'Defective', hãy chắc chắn DB lưu đúng như vậy
        if (item.status === 'Defective' || item.status === 'DEFECTIVE') {
            defectiveCount++;
        }
    });

    // --- Render ra HTML ---

    // 1. Tổng Tồn
    document.getElementById('kpi-total-qty').textContent = totalQty.toLocaleString();

    // 2. Giá Trị Tồn (Format tiền tệ VNĐ)
    document.getElementById('kpi-total-value').textContent = totalValue.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });

    // 3. Sắp Hết Hàng
    document.getElementById('kpi-low-stock').textContent = lowStockCount;
    // Tô màu đỏ nếu có hàng sắp hết
    if (lowStockCount > 0) {
        document.getElementById('kpi-low-stock').classList.add('text-danger');
    }

    // 4. Hàng Lỗi
    document.getElementById('kpi-defective').textContent = defectiveCount;
    if (defectiveCount > 0) {
        document.getElementById('kpi-defective').classList.add('text-danger');
    }
}

function updatePagination(pageData) {
    document.getElementById('page-info').textContent = `Trang ${pageData.number + 1} / ${pageData.totalPages}`;
    document.getElementById('prev-page').disabled = pageData.first;
    document.getElementById('next-page').disabled = pageData.last;
}

// --- LOGIC BỘ LỌC (CLIENT-SIDE) ---
const searchInput = document.getElementById('search-part'); // Ô tìm kiếm
const locationInput = document.getElementById('filter-location'); // Dropdown kho
const statusSelect = document.getElementById('filter-status'); // Dropdown trạng thái (đang bị lỗi tên ID, bạn check lại HTML xem ID là filter-status hay gì)

function applyFilter() {
    // 1. Lấy giá trị từ các ô input
    const keyword = searchInput.value.toLowerCase();
    const loc = locationInput.value; // Giá trị kho (101, 102...)
    // Lưu ý: Trong HTML bạn gửi ảnh, dropdown trạng thái có text là "Lỗi",
    // bạn cần kiểm tra xem <option value="..."> của nó là gì.
    // Giả sử value="Defective" cho Lỗi, "Available" cho Sẵn sàng.
    const st = statusSelect.value;

    console.log("🔍 Đang lọc:", { keyword, loc, st });

    // 2. Lọc trên dữ liệu gốc (allInventoryData)
    // Lưu ý: Biến allInventoryData phải được gán giá trị lúc load API đầu tiên
    // (Trong hàm loadInventoryTable, nhớ thêm dòng: allInventoryData = data;)

    const filtered = allInventoryData.filter(item => {
        const pName = (item.partName || '').toLowerCase();
        const pSku = (item.serialNumber || '').toLowerCase();
        const iLoc = (item.location || '').toString(); // Chuyển location thành chuỗi để so sánh

        const matchName = !keyword || pName.includes(keyword) || pSku.includes(keyword);
        const matchLoc = !loc || iLoc.includes(loc);
        const matchStatus = !st || item.status === st;

        return matchName && matchLoc && matchStatus;
    });

    // 3. Render lại bảng với dữ liệu đã lọc
    renderTable(filtered);
}

// Gắn sự kiện
searchInput.addEventListener('input', applyFilter);
// Với thẻ <select>, dùng sự kiện 'change'
if(locationInput) locationInput.addEventListener('change', applyFilter);
if(statusSelect) statusSelect.addEventListener('change', applyFilter);

// Logic cho kho sau khi approve claim:
function setupTabs() {
    const tabs = document.querySelectorAll('.tab-btn');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            // 1. Active UI Tab
            document.querySelectorAll('.tab-btn').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');

            // 2. Show Content
            const targetId = tab.dataset.tab; // 'stock' hoặc 'allocation'
            document.getElementById('tab-stock').style.display = targetId === 'stock' ? 'block' : 'none';
            document.getElementById('tab-allocation').style.display = targetId === 'allocation' ? 'block' : 'none';

            // 3. Load Data nếu cần
            if (targetId === 'allocation') {
                loadAllocationTable();
            }
        });
    });
}

// Biến toàn cục để lưu trang hiện tại của tab Allocation
let currentAllocationPage = 0;

async function loadAllocationTable() {
    const tbody = document.getElementById('allocation-tbody');
    tbody.innerHTML = '<tr><td colspan="8" class="text-center py-5"><div class="spinner-border text-primary"></div> Đang tải lịch sử...</td></tr>';

    try {
        // 1. Gọi API Backend (Đã sửa ở trên)
        const response = await InventoryService.getAllAllocations(currentAllocationPage, 10);
        const data = response.content || [];

        // 2. Kiểm tra dữ liệu rỗng
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-4">Chưa có lịch sử cấp phát nào.</td></tr>';
            return;
        }

        // 3. Render dữ liệu
        tbody.innerHTML = ''; // Xóa loading

        data.forEach(item => {
            // Format ngày tháng
            const date = item.allocatedDate ? new Date(item.allocatedDate).toLocaleString('vi-VN') : '-';

            // Xử lý thông tin phụ tùng (item.inventory có thể null nếu query join không fetch)
            // Tùy vào DTO trả về, ở đây giả định PartAllocationResponse có field partName hoặc inventory object
            const partName = item.partName || 'Unknown Part';
            const partSku = item.serialNumber || 'N/A';
            const location = item.inventoryId || 'N/A';

            // Badge trạng thái
            let statusClass = 'bg-secondary';
            let statusText = item.status;

            if (item.status === 'PENDING') {
                statusClass = 'bg-warning text-dark';
                statusText = 'Chờ xử lý';
            } else if (item.status === 'WAITING_FOR_PART') {
                statusClass = 'bg-danger';
                statusText = 'Thiếu hàng';
            } else if (item.status === 'READY_TO_INSTALL') {
                statusClass = 'bg-primary';
                statusText = 'Sẵn sàng lắp';
            } else if (item.status === 'COMPLETED') { // Nếu có
                statusClass = 'bg-success';
                statusText = 'Hoàn tất';
            }

            tbody.innerHTML += `
                <tr>
                    <td>#${item.allocationId}</td>
                    <td class="fw-bold text-primary">${item.claimCode}</td>
                    <td>
                        <div class="fw-bold">${partName}</div>
                        <small class="text-muted">SKU: ${partSku}</small>
                    </td>
                    <td><span class="badge bg-light text-dark border">Trạm ${item.serviceCenterId}</span></td>
                    <td class="fw-bold">${item.allocatedQty}</td>
                    <td>${location}</td>
                    <td class="small text-muted">${date}</td>
                    <td><span class="badge ${statusClass}">${statusText}</span></td>
                </tr>
            `;
        });

        // (Tùy chọn) Cập nhật phân trang nếu bạn muốn làm kỹ phần này
        // updatePagination(response);

    } catch (error) {
        console.error(error);
        tbody.innerHTML = `<tr><td colspan="8" class="text-danger text-center">Lỗi tải dữ liệu: ${error.message}</td></tr>`;
    }
}