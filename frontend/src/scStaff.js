// src/scStaff.js

// Import các "linh kiện" và "tiện ích"
import { checkAuth } from './utils/auth.js';
import { renderHeader } from './components/Header.js';
import { renderStaffSidebar } from './components/StaffSidebar.js';
import { getUser } from "./utils/storage.js";
import { getUsersByRole} from "./services/userService.js";
import { getClaims, createClaim } from './services/warrantyService.js';
import { logout } from './services/authService.js'; // (Bạn cần tạo file authService.js)
import { getVehicleByVin, getVehicleHistory } from './services/vehicleService.js';
import { initVehicleDetailPage } from './JS/sc-vehicle-detail.js';
import { initVehicleHistoryPage } from './JS/sc-vehicle-history.js';
import { initAppointmentsPage } from './JS/sc-appointments.js';
// --- LOGIC CHO TỪNG TRANG CỤ THỂ ---
// (Kiểm tra xem file HTML nào đang gọi file JS này)

// 3. Logic cho trang Dashboard (index.html)
if (window.location.pathname.endsWith('/scStaff/index.html')) {
    loadDashboardData();
}

// 4. Logic cho trang "Tạo Claim"
if (window.location.pathname.endsWith('/scStaff/create-claim.html')) {
    setupCreateClaimForm();
}


// --- Các hàm thực thi ---

/**
 * Tải dữ liệu cho Dashboard và Bảng (Trang index.html)
 */
async function loadDashboardData() {
    try {
        const params = {
            page: 0,
            size: 10,
            status: 'WAITING_APPROVAL' // Chỉ lấy claim đang chờ
        };
        const data = await getClaims(params); // Gọi API

        // 1. Cập nhật ô tóm tắt
        document.getElementById('claims-pending-count').textContent = data.totalElements;

        // 2. Vẽ lại bảng
        const tbody = document.getElementById("claims-table-body");
        tbody.innerHTML = ''; // Xóa dữ liệu cũ

        if (data.empty) {
            tbody.innerHTML = '<tr><td colspan="4">Không có claim nào.</td></tr>';
            return;
        }

        data.content.forEach(claim => {
            // Định dạng ngày cho dễ đọc
            const dateCreated = new Date(claim.dateCreated).toLocaleString('vi-VN');

            // Cắt ngắn mô tả (nếu có)
            const shortDescription = claim.description && claim.description.length > 50
                ? claim.description.substring(0, 50) + '...'
                : (claim.description || '(Không có mô tả)');

            tbody.innerHTML += `
                <tr>
                    <td>
                        <a href="/pages/scStaff/claim-details.html?id=${claim.id}" 
                           title="Xem chi tiết ${claim.claimCode}">
                           <strong>${claim.claimCode}</strong>
                        </a>
                    </td>
                    <td>${claim.vin}</td>
                    <td>${dateCreated}</td>
                    <td>${shortDescription}</td>
                    <td><span className="status-${claim.currentStatus.toLowerCase()}">${claim.currentStatus}</span></td>
                    <td>
                        <a href="/pages/scStaff/claim-details.html?id=${claim.id}" 
                           class="btn btn-sm btn-outline-primary">
                           Xem
                        </a>
                    </td>
                </tr>
            `;
        });



    } catch (error) {
        alert('Lỗi tải dashboard: ' + error.message);
    }
}

/**
 * Gắn sự kiện cho Form (Trang create-claim.html)
 */
// Cap nhat sua doi khi tao trang create-claim  23:57, 13/11/25
/**
 * Gắn sự kiện VÀ Tải dữ liệu cho Form (Trang create-claim.html)
 * (Đây là phiên bản NÂNG CẤP)
 */
async function setupCreateClaimForm() {
    const form = document.getElementById('claim-form');
    // Kiểm tra an toàn, nếu không phải trang create-claim thì dừng
    if (!form) return;

    console.log("Đang khởi tạo trang Tạo Claim...");

    const technicianSelect = document.getElementById('technicianId');
    const button = document.getElementById('create-claim-btn');
    const errorEl = document.getElementById('form-error');

    // --- 1. Tải danh sách Kỹ thuật viên (MỚI) ---
    try {
        const technicians = await getUsersByRole('SC_TECHNICIAN');

        technicianSelect.innerHTML = ''; // Xóa chữ "Đang tải..."

        if (!technicians || technicians.length === 0) {
            technicianSelect.add(new Option('Không tìm thấy Kỹ thuật viên nào', ''));
            technicianSelect.disabled = true;
        } else {
            technicianSelect.add(new Option('-- Chọn Kỹ thuật viên --', ''));
            technicians.forEach(tech => {
                // (API trả về fullName và userId)
                technicianSelect.add(new Option(tech.fullName, tech.userId));
            });
        }
    } catch (error) {
        console.error("Lỗi tải danh sách KTV:", error);
        technicianSelect.innerHTML = `<option value="">Lỗi tải danh sách KTV</option>`;
        technicianSelect.disabled = true;
    }

    // --- 2. Gắn sự kiện Submit ---
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        button.disabled = true;
        button.textContent = 'Đang gửi...';
        errorEl.textContent = '';

        try {
            // 1. Lấy giá trị từ form
            const vin = document.getElementById('vin').value;
            const description = document.getElementById('description').value;
            const technicianId = document.getElementById('technicianId').value;

            if (!technicianId) {
                throw new Error("Bạn chưa chọn Kỹ thuật viên");
            }

            // 2. (Tạm thời hard-code, chúng ta sẽ nâng cấp phần này sau)
            const requestedParts = [{ partNumber: "PN-DEMO", quantity: 1 }];
            const attachedDocuments = [];
            const isRecall = false;

            // 3. Tạo đối tượng DTO hoàn chỉnh
            const createClaimDto = {
                vin,
                description,
                technicianId: parseInt(technicianId), // Chuyển sang Long/Number
                isRecall,
                requestedParts,
                attachedDocuments
            };

            // 4. Gọi API
            const newClaimId = await createClaim(createClaimDto);

            alert('Tạo Claim thành công! ID mới là: ' + newClaimId);
            window.location.href = '/pages/scStaff/index.html'; // Chuyển về trang dashboard

        } catch (error) {
            console.error('Lỗi tạo claim:', error);
            errorEl.textContent = 'Lỗi: ' + error.message;
            button.disabled = false;
            button.textContent = 'Gửi Yêu cầu';
        }
    });
}

// ================== Cap nhat file scStaff.js khi lam trang claim-list.html =================== 23:14, 13/11/25

// --- BIẾN TRẠNG THÁI CHO TRANG CLAIM LIST ---
// (Lưu trữ bộ lọc và trang hiện tại)
let claimListState = {
    currentPage: 0,
    size: 10,
    vin: '',
    claimCode: '',
    status: ''
};

// --- HÀM LOGIC CHO TRANG DANH SÁCH CLAIM (MỚI) ---

/**
 * Hàm chính để khởi tạo trang Danh sách Claim
 */
function initClaimListPage() {
    console.log("Đang khởi tạo trang Danh sách Claim...");

    // Gắn sự kiện cho các nút lọc
    document.getElementById('filter-apply-btn').addEventListener('click', applyFilters);
    document.getElementById('filter-clear-btn').addEventListener('click', clearFilters);

    // Tải dữ liệu lần đầu
    fetchAndRenderClaims();
}

/**
 * (Hàm chính) Gọi API và vẽ lại Bảng + Phân trang
 */
async function fetchAndRenderClaims() {
    const loadingEl = document.getElementById('claims-loading');
    const errorEl = document.getElementById('claims-error');
    const tableContainerEl = document.getElementById('claims-table-container');
    const noClaimsEl = document.getElementById('no-claims-message');
    const paginationEl = document.getElementById('pagination-container');

    try {
        // 1. Hiển thị loading, ẩn mọi thứ
        loadingEl.classList.remove('d-none');
        errorEl.classList.add('d-none');
        tableContainerEl.classList.add('d-none');
        noClaimsEl.classList.add('d-none');
        paginationEl.classList.add('d-none');

        // 2. Lấy tham số lọc từ state
        const params = {
            page: claimListState.currentPage,
            size: claimListState.size,
            vin: claimListState.vin,
            claimCode: claimListState.claimCode,
            status: claimListState.status
        };

        // 3. Gọi API
        const claimsPage = await getClaims(params);

        // 4. Ẩn loading
        loadingEl.classList.add('d-none');

        // 5. Render Bảng
        renderClaimsTable(claimsPage.content);

        // 6. Render Phân trang
        renderPagination(claimsPage); // Gửi toàn bộ đối tượng Page

        // 7. Xử lý trường hợp không có dữ liệu
        if (claimsPage.empty) {
            noClaimsEl.classList.remove('d-none');
        } else {
            tableContainerEl.classList.remove('d-none');
            paginationEl.classList.remove('d-none');
        }

    } catch (error) {
        console.error('Lỗi khi tải danh sách claims:', error);
        loadingEl.classList.add('d-none');
        errorEl.textContent = `Không thể tải dữ liệu. Lỗi: ${error.message}`;
        errorEl.classList.remove('d-none');
    }
}

/**
 * Vẽ Bảng
 */
function renderClaimsTable(claims = []) {
    const tbodyEl = document.getElementById('claims-tbody');
    tbodyEl.innerHTML = ''; // Xóa dữ liệu cũ

    if (claims.length === 0) return;

    claims.forEach(claim => {
        const tr = document.createElement('tr');
        const dateCreated = new Date(claim.dateCreated).toLocaleString('vi-VN');

        // (Đây là CSS tùy chỉnh, bạn có thể thêm vào dashboard.css)
        const statusClass = `status-${(claim.currentStatus || 'default').toLowerCase()}`;

        tr.innerHTML = `
            <td>
                <a href="/pages/scStaff/claim-details.html?id=${claim.id}" title="Xem chi tiết">
                    <strong>${claim.claimCode}</strong>
                </a>
            </td>
            <td>${claim.vin}</td>
            <td>${claim.customerName || '(Chưa có)'}</td>
            <td>${dateCreated}</td>
            <td><span class="badge ${statusClass}">${claim.currentStatus}</span></td>
            <td>
                <a href="/pages/scStaff/claim-details.html?id=${claim.id}" 
                   class="btn btn-sm btn-outline-primary">
                   Xem
                </a>
            </td>
        `;
        tbodyEl.appendChild(tr);
    });
}

/**
 * Vẽ Phân trang (Pagination)
 */
function renderPagination(pageData) {
    const paginationControls = document.getElementById('pagination-controls');
    paginationControls.innerHTML = ''; // Xóa các nút cũ

    const { totalPages, number: currentPage, first, last } = pageData;

    if (totalPages <= 1) return; // Không cần phân trang nếu chỉ có 1 trang

    // Nút "Trang trước"
    paginationControls.innerHTML += `
        <li class="page-item ${first ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage - 1}">Trang trước</a>
        </li>
    `;

    // Hiển thị các nút số trang
    // (Đây là logic cơ bản, có thể làm phức tạp hơn với dấu "...")
    for (let i = 0; i < totalPages; i++) {
        paginationControls.innerHTML += `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i}">${i + 1}</a>
            </li>
        `;
    }

    // Nút "Trang sau"
    paginationControls.innerHTML += `
        <li class="page-item ${last ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage + 1}">Trang sau</a>
        </li>
    `;

    // Gắn sự kiện click cho các nút phân trang MỚI
    paginationControls.querySelectorAll('a.page-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            if (link.parentElement.classList.contains('disabled')) return;

            const newPage = parseInt(link.getAttribute('data-page'));
            claimListState.currentPage = newPage; // Cập nhật state
            fetchAndRenderClaims(); // Gọi lại API với trang mới
        });
    });
}

/**
 * Xử lý sự kiện khi bấm nút "Lọc"
 */
function applyFilters() {
    // 1. Cập nhật state từ các ô input
    claimListState.vin = document.getElementById('filter-vin').value.trim();
    claimListState.claimCode = document.getElementById('filter-claim-code').value.trim();
    claimListState.status = document.getElementById('filter-status').value;
    claimListState.currentPage = 0; // Luôn reset về trang đầu tiên khi lọc

    // 2. Gọi lại API
    fetchAndRenderClaims();
}

/**
 * Xử lý sự kiện khi bấm nút "Xóa lọc"
 */
function clearFilters() {
    // 1. Reset state
    claimListState.vin = '';
    claimListState.claimCode = '';
    claimListState.status = '';
    claimListState.currentPage = 0;

    // 2. Reset giá trị trên UI
    document.getElementById('filter-vin').value = '';
    document.getElementById('filter-claim-code').value = '';
    document.getElementById('filter-status').value = '';

    // 3. Gọi lại API
    fetchAndRenderClaims();
}



// ================= Ham main su dung chung ================== 23:15, 13/11/25

// --- HÀM "ROUTER" CHÍNH ---
// (Bạn cần CẬP NHẬT hàm main() của bạn để bao gồm logic mới)

function main() {
    // 1. Gác cổng (Bạn đã có)
    const user = checkAuth('ROLE_SC_STAFF');
    if (!user) return;

    // 2. Render các component chung (Bạn đã có)
    try {
        renderHeader('header-placeholder');
        renderStaffSidebar('sidebar-placeholder');
    } catch (e) {
        console.warn("Chưa có hàm renderHeader/renderStaffSidebar. Bỏ qua...", e);
        // Fallback đơn giản
        document.getElementById('header-placeholder').innerHTML =
            `<nav class="navbar navbar-light bg-white p-3 shadow-sm">
                <span class="navbar-brand mb-0 h1">EV Warranty</span>
                <span class="text-dark">Chào, ${user.username}!</span>
             </nav>`;
    }

    // 3. Chạy logic cho trang cụ thể dựa trên ID của body
    const bodyId = document.body.id;

    if (bodyId === 'sc-dashboard-page') {
        // (Đây là code từ Task 1, bạn đã có)
        // initDashboardPage();
    }
    // === PHẦN MỚI ===
    else if (bodyId === 'sc-claim-list-page') {
        initClaimListPage(); // Gọi hàm khởi tạo trang mới
    }

    if (bodyId === 'sc-vehicle-detail-page') {
        initVehicleDetailPage();
      }



    // (Bạn cũng có thể dùng logic cũ của bạn)
    if (window.location.pathname.endsWith('/scStaff/index.html')) {
        loadDashboardData();
    }

    if (window.location.pathname.endsWith('/scStaff/vehicle-history.html')) {
      initVehicleHistoryPage();
    }

    else if (window.location.pathname.endsWith('/scStaff/appointments.html')) {
      initAppointmentsPage();
    }


    // NÓ SẼ GỌI HÀM NÀY KHI Ở TRANG create-claim.html
    if (bodyId === 'sc-create-claim-page') {
        setupCreateClaimForm(); // <== GỌI HÀM MỚI
    }
}

// Chạy hàm main khi DOM đã sẵn sàng
document.addEventListener('DOMContentLoaded', main);


