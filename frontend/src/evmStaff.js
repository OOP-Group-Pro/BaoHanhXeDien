/* src/js/evmStaff.js (File "Não" cho EVM Staff) */

// (SỬA LỖI 1: Sửa đường dẫn import, thêm ../)
import { checkAuth } from 'utils/auth.js';
import { renderHeader } from 'components/Header.js';

import { logout } from './services/authService.js'; // (Đường dẫn này ĐÚNG)
import { renderEvmSidebar } from 'components/EvmSidebar.js';
import { getClaims, createClaim, rejectClaim, approveClaim, getClaimDetails, getClaimHistory } from './services/warrantyService.js'; // (Đường dẫn này ĐÚNG)
// (Import partService nếu cần gọi API Part)
// import { getPartDetails } from './services/partService.js';


// --- (SỬA LỖI 2: BỌC TẤT CẢ LOGIC VÀO HÀM "startApp") ---
function startApp() {
    // 1. GÁC CỔNG:

    const userInfo = checkAuth(['ROLE_ADMIN', 'ROLE_EVM_STAFF']);

    if (!userInfo) {
        console.error("[AUTH] Xác thực thất bại, dừng ứng dụng.");
        return; // <-- Bây giờ nó hợp lệ vì nằm BÊN TRONG hàm startApp()
    }

    console.log('EVM Staff Authenticated:', userInfo);

    // Render header and sidebar after DOM is ready so placeholders exist
    document.addEventListener('DOMContentLoaded', () => {
        // render shared UI into placeholders
        try { renderHeader(); } catch (e) { console.warn('renderHeader() failed:', e); }
        try { renderEvmSidebar(); } catch (e) { console.warn('renderEvmSidebar() failed:', e); }

        const path = window.location.pathname;

        // (SỬA LỖI 3: Thêm kiểm tra /EVMStaff/ (viết hoa) VÀ /claims.html)
        if (path.endsWith('/EVMStaff/claims.html') || path.endsWith('/claims.html')) {
            console.log("DEBUG: Đã khớp đường dẫn claims.html, đang gọi initializeClaimsListPage()...");
            initializeClaimsListPage();
        }

        // 4. Logic cho trang CHI TIẾT CLAIM (Giao diện 2)
        if (path.endsWith('/EVMStaff/claim-detail.html') || path.endsWith('/claim-detail.html')) {
            initializeClaimDetailPage();
        }
    });
}

// --- GỌI HÀM ĐỂ KHỞI CHẠY ---
startApp();


// --- CÁC HÀM THỰC THI (CHO TRANG DANH SÁCH) ---
// (Phần code bên dưới đã đúng, giữ nguyên)

function initializeClaimsListPage() {
    console.log("DEBUG: initializeClaimsListPage() ĐANG CHẠY!"); // (Thêm log để kiểm tra)

    // ... (code lấy element giữ nguyên)
    const statusFilter = document.getElementById('status-filter');
    const tableBody = document.getElementById('claims-table-body');
    const modal = document.getElementById('create-claim-modal');
    const openModalBtn = document.getElementById('open-create-claim-modal');
    const closeModalBtn = document.getElementById('close-claim-modal');
    const cancelModalBtn = document.getElementById('cancel-claim-modal');
    const createClaimForm = document.getElementById('create-claim-form');

    // (Thêm kiểm tra null để debug)
    if (!openModalBtn) {
        console.error("LỖI: Không tìm thấy nút #open-create-claim-modal");
        return;
    }
    if (!modal) {
        console.error("LỖI: Không tìm thấy modal #create-claim-modal");
        return;
    }

    // Gán sự kiện cho filter
    statusFilter.addEventListener('change', (e) => {
        fetchClaims(e.target.value);
    });

    // ... (code sự kiện click table giữ nguyên)
    tableBody.addEventListener('click', (e) => {
        e.preventDefault();
        const target = e.target;
        const claimId = target.getAttribute('data-id');
        if (!claimId) return;

        // Xử lý nút [Từ chối]
        if (target.classList.contains('action-link-danger')) {
            const reason = prompt(`Nhập lý do từ chối cho Claim ID: ${claimId}`);
            if (reason) handleRejectClaim(claimId, reason);
        }

        // Xử lý nút [Xem chi tiết] -> Mở trang Phê duyệt (Giao diện 2)
        if (target.classList.contains('action-link') && !target.classList.contains('action-link-danger')) {
            window.location.href = `/pages/EVMStaff/claim-detail.html?id=${claimId}`; // (Sửa lại đường dẫn viết hoa)
        }
    });

    // Gán sự kiện cho Modal (Bây giờ sẽ chạy được)
    openModalBtn.addEventListener('click', () => {
        console.log("DEBUG: Nút 'Tạo Claim Mới' đã được click!");
        modal.classList.add('show');
    });
    closeModalBtn.addEventListener('click', () => modal.classList.remove('show'));
    cancelModalBtn.addEventListener('click', () => modal.classList.remove('show'));
    modal.addEventListener('click', (e) => {
        if (e.target === modal) modal.classList.remove('show');
    });
    createClaimForm.addEventListener('submit', handleCreateClaim);

    // Tải dữ liệu lần đầu
    fetchClaims('WAITING_APPROVAL');
}

/**
 * (Async) Hàm gọi API Lấy Danh sách Claim (Giao diện 1)
 */
async function fetchClaims(status = 'PENDING') {
// ... (Hàm này giữ nguyên)
    const tableBody = document.getElementById('claims-table-body');
    tableBody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 40px;">Đang tải dữ liệu...</td></tr>';

    try {
        const params = { status: status, page: 0, size: 20 };
        const data = await getClaims(params);
        populateTable(data.content || data);

    } catch (error) {
        console.error("Lỗi khi fetch dữ liệu:", error);
        tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: red; padding: 40px;">Lỗi khi tải dữ liệu: ${error.message}</td></tr>`;
    }
}

/**
 * Hàm Vẽ Bảng (Giao diện 1)
 */
function populateTable(data) {
// ... (Hàm này giữ nguyên)
    const tableBody = document.getElementById('claims-table-body');
    tableBody.innerHTML = '';

    if (!data || data.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 40px;">Không tìm thấy Claim nào.</td></tr>';
        return;
    }

    data.forEach(item => {
        const row = document.createElement('tr');
        const formattedDate = item.dateCreated ? new Date(item.dateCreated).toLocaleDateString('vi-VN') : 'N/A';

        row.innerHTML = `
            <td>
                <span class="primary-text">${item.claimCode || 'N/A'}</span>
                <span class="secondary-text">VIN: ${item.vin}</span>
            </td>
            <td>
                <span class="primary-text">${item.model || 'N/A'}</span> 
                <span class="secondary-text">KH: ${item.customerName || 'N/A'}</span>
            </td>
            <td>
                <span class="primary-text">${item.scName || 'N/A'}</span>
            </td>
            <td>
                <span class="status-badge status-${item.currentStatus.toLowerCase()}">${item.currentStatus}</span>
            </td>
            <td>${formattedDate}</td>
            <td>
                <a href="#" class="action-link" data-id="${item.claimId || item.id}">[Xem chi tiết]</a>
                <a href="#" class="action-link action-link-danger" data-id="${item.claimId || item.id}">[Từ chối]</a>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

/**
 * (Async) Hàm gọi API Tạo Claim Mới (từ Modal)
 */
async function handleCreateClaim(event) {
// ... (Hàm này giữ nguyên)
    event.preventDefault();
    const submitClaimBtn = document.getElementById('submit-claim-btn');
    const claimErrorMessage = document.getElementById('claim-error-message');

    setButtonLoading(submitClaimBtn, true, "Đang lưu...");
    claimErrorMessage.style.display = 'none';

    // 1. Lấy dữ liệu từ Form (khớp với CreateClaimDto.java)
    const formData = new FormData(document.getElementById('create-claim-form'));

    const requestedPartsList = formData.get('requestedParts').split(',')
        .map(partNum => partNum.trim())
        .filter(partNum => partNum.length > 0)
        .map(partNum => ({ partNumber: partNum, quantity: 1 }));

    const claimData = {
        vin: formData.get('vin'),
        description: formData.get('description'),
        technicianId: formData.get('technicianId') ? parseInt(formData.get('technicianId')) : null,
        isRecall: formData.get('isRecall') === 'on',
        requestedParts: requestedPartsList,
        attachedDocuments: []
    };

    // 2. Gọi API POST /api/v1/claims
    try {
        const newClaimId = await createClaim(claimData);

        alert(`Tạo Claim thành công! Mã Claim ID: ${newClaimId}`);
        document.getElementById('create-claim-modal').classList.remove('show');
        fetchClaims(document.getElementById('status-filter').value); // Tải lại danh sách

    } catch (error) {
        console.error("Lỗi khi tạo Claim:", error);
        claimErrorMessage.textContent = `Lỗi: ${error.message}`;
        claimErrorMessage.classList.add('show');
    } finally {
        setButtonLoading(submitClaimBtn, false, "Lưu Claim");
    }
}

/**
 * (Async) Hàm gọi API Từ chối Claim (Giao diện 1)
 */
async function handleRejectClaim(claimId, reason) {
// ... (Hàm này giữ nguyên)
    console.log(`Đang gọi API Từ chối cho ${claimId} với lý do: ${reason}`);

    try {
        await rejectClaim(claimId, reason);

        alert("Đã từ chối Claim thành công!");
        fetchClaims(document.getElementById('status-filter').value); // Tải lại bảng

    } catch(error) {
        alert(`Lỗi: ${error.message}`);
    }
}

/**
 * Hàm Helper - Vô hiệu hóa nút
 */
function setButtonLoading(button, isLoading, text = "Lưu Claim") {
// ... (Hàm này giữ nguyên)
    if (button) {
        button.disabled = isLoading;
        button.textContent = isLoading ? text.replace("Lưu", "Đang lưu") : text;
    }
}


// --- CÁC HÀM THỰC THI (CHO TRANG CHI TIẾT) ---

function initializeClaimDetailPage() {
// ... (Hàm này giữ nguyên)
    // 1. Lấy Claim ID từ URL
    const urlParams = new URLSearchParams(window.location.search);
    const claimId = urlParams.get('id');

    if (!claimId) {
        document.getElementById('main-content').innerHTML = '<h1>Lỗi: Không tìm thấy Claim ID</h1>';
        return;
    }

    // 2. Lấy các Nút hành động
    const approveButton = document.getElementById('approve-btn');
    const rejectButton = document.getElementById('reject-btn');

    // 3. Gán sự kiện
    // (Thêm kiểm tra null, vì trang claims.html không có 2 nút này)
    if (approveButton && rejectButton) {
        approveButton.addEventListener('click', () => handleApprove(claimId));
        rejectButton.addEventListener('click', () => handleReject(claimId));
    }


    // 4. Tải dữ liệu
    loadClaimDetails(claimId);
    loadClaimHistory(claimId);
}

/**
 * (Async) Tải chi tiết Claim (Giao diện 2)
 */
async function loadClaimDetails(claimId) {
// ... (Hàm này giữ nguyên)
    const dataContainer = document.getElementById('claim-data-container');
    // (Thêm kiểm tra null)
    if (!dataContainer) return;

    dataContainer.innerHTML = '<div class="loading-spinner"><i class="fa-solid fa-spinner fa-spin"></i></div>';

    try {
        const claim = await getClaimDetails(claimId);

        // (Giả lập dữ liệu ảnh)
        const mockImages = [
            'https://i.imgur.com/gT6HqgB.jpeg', // Placeholder ảnh pin
            'https://i.imgur.com/zWbL5pE.png'  // Placeholder ảnh màn hình
        ];
        let imageHtml = '';
        mockImages.forEach(url => {
            imageHtml += `<img src="${url}" alt="Ảnh đính kèm" onerror="this.src='https://via.placeholder.com/150x100?text=Image+Error'">`;
        });

        // 2. Hiển thị dữ liệu
        dataContainer.innerHTML = `
            <div class="detail-item">
                <label>VIN</label>
                <div class="value">${claim.vin || 'N/A'}</div>
            </div>
            <div class="detail-item">
                <label>Trạng thái Hiện tại</label>
                <div class="value">${claim.currentStatus || 'N/A'}</div>
            </div>
            <div class="detail-item">
                <label>Khách hàng (Từ Vehicle-Service)</label>
                <div class="value">${claim.customerName || 'N/A'}</div>
            </div>
            <div class="detail-item">
                <label>Mô tả sự cố</label>
                <div class="value">${claim.description || 'N/A'}</div>
            </div>
            <div class="detail-item">
                <label>Ảnh/bằng chứng đính kèm (Giả lập)</label>
                <div class="image-gallery">${imageHtml}</div>
            </div>
        `;

        // 3. Hiển thị nút bấm
        const actionButtons = document.getElementById('action-buttons');
        if (actionButtons) { // (Thêm kiểm tra null)
            if (claim.currentStatus === 'WAITING_APPROVAL') {
                actionButtons.style.display = 'flex';
            } else {
                actionButtons.innerHTML = `<p style="color:var(--status-approved-text); font-weight: 600;">Claim này đã được xử lý (Trạng thái: ${claim.currentStatus})</p>`;
                actionButtons.style.display = 'flex';
            }
        }

    } catch (error) {
        dataContainer.innerHTML = `<div class="error-message show">Lỗi: ${error.message}</div>`;
    }
}

/**
 * (Async) Tải Lịch sử Claim (Giao diện 2)
 */
async function loadClaimHistory(claimId) {
// ... (Hàm này giữ nguyên)
    const historyContainer = document.getElementById('history-data-container');
    // (Thêm kiểm tra null)
    if (!historyContainer) return;

    historyContainer.innerHTML = '<div class="loading-spinner"><i class="fa-solid fa-spinner fa-spin-pulse"></i></div>';

    try {
        const historyList = await getClaimHistory(claimId);

        historyContainer.innerHTML = ''; // Xóa loading
        if (!historyList || historyList.length === 0) {
            historyContainer.innerHTML = '<span class="subtext">Chưa có lịch sử.</span>';
            return;
        }

        historyList.forEach(log => {
            const item = document.createElement('div');
            item.className = 'data-item';
            item.innerHTML = `
                <label>${new Date(log.timestamp).toLocaleString('vi-VN')}</label>
                <div class="value" style="font-size: 1rem; color: var(--primary-blue);">${log.status}</div>
                <span class="subtext">Bởi: ${log.processorName || 'Hệ thống'}</span>
                ${log.notes ? `<span class="subtext" style="font-style: italic;">Ghi chú: ${log.notes}</span>` : ''}
            `;
            historyContainer.appendChild(item);
        });

    } catch (e) {
        historyContainer.innerHTML = `<span style="color:var(--danger-red)">Lỗi tải lịch sử</span>`;
    }
}

/**
 * (Async) Hàm gọi API Phê duyệt (Giao diện 2)
 */
async function handleApprove(claimId) {
// ... (Hàm này giữ nguyên)
    const notes = prompt("Thêm ghi chú phê duyệt (không bắt buộc):");
    if (notes === null) return; // Người dùng nhấn Hủy

    setButtonLoading(document.getElementById('approve-btn'), true, "Đang duyệt...");
    setButtonLoading(document.getElementById('reject-btn'), true);

    try {
        await approveClaim(claimId, notes || '');

        alert("Đã phê duyệt Claim thành công!");
        loadClaimDetails(claimId); // Tải lại chi tiết
        loadClaimHistory(claimId); // Tải lại lịch sử

    } catch (error) {
        alert(`Lỗi: ${error.message}`);
    } finally {
        // (Không cần reset nút vì logic hiển thị nút sẽ tự động ẩn chúng đi)
    }
}

/**
 * (Async) Hàm gọi API Từ chối (Giao diện 2)
 */
async function handleReject(claimId) {
// ... (Hàm này giữ nguyên)
    const reason = prompt("Vui lòng nhập LÝ DO TỪ CHỐI (bắt buộc):");
    if (!reason || reason.trim() === '') {
        if (reason !== null) alert("Bạn phải nhập lý do từ chối.");
        return;
    }

    setButtonLoading(document.getElementById('approve-btn'), true);
    setButtonLoading(document.getElementById('reject-btn'), true, "Đang từ chối...");

    try {
        await rejectClaim(claimId, reason);

        alert("Đã từ chối Claim thành công!");
        loadClaimDetails(claimId); // Tải lại chi tiết
        loadClaimHistory(claimId); // Tải lại lịch sử

    } catch (error) {
        alert(`Lỗi: ${error.message}`);
    }
}