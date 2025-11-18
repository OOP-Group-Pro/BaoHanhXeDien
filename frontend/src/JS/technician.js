// src/js/technician.js

// Import các "linh kiện" và "tiện ích"
import { checkAuth } from '../utils/auth.js';
import { renderHeader } from '../components/Header.js';
import { renderTechnicianSidebar } from '../components/TechnicianSidebar.js';
import { getUser } from "../utils/storage.js";
import { api } from '../services/apiClient.js';
import '../styles/technician.css';

// --- BIẾN TRẠNG THÁI ---
let currentClaimData = null;
let currentTechnicianId = null;

// --- HÀM "ROUTER" CHÍNH - CHẠY NGAY KHI LOAD ---
(function main() {
    console.log('🚀 Technician.js đã load!');

    // 1. Gác cổng - Yêu cầu role SC_TECHNICIAN
    const user = checkAuth('ROLE_SC_TECHNICIAN');
    if (!user) {
        console.error('❌ Không có quyền truy cập!');
        return;
    }

    console.log('✅ User authenticated:', user);

    // Lưu ID của technician hiện tại
    currentTechnicianId = user.id || user.sub;

    // 2. Render các component chung
    console.log('📌 Đang render Header và Sidebar...');

    try {
        renderHeader();
        renderTechnicianSidebar();
        console.log('✅ Header và Sidebar đã render!');
    } catch (e) {
        console.error('❌ Lỗi render components:', e);
    }

    // 3. Khởi tạo trang sau khi DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initJobListPage);
    } else {
        initJobListPage();
    }
})();

/**
 * Khởi tạo trang Danh sách công việc
 */
function initJobListPage() {
    console.log("📋 Đang khởi tạo trang Danh sách công việc...");

    // Lấy các phần tử DOM
    const jobTableBody = document.getElementById('job-table-body');
    const btnFinishRepair = document.getElementById('btn-finish-repair');
    const reportForm = document.getElementById('report-form');
    const confirmCheckbox = document.getElementById('confirm-checkbox');
    const btnSubmitReport = document.getElementById('btn-submit-report');
    const backButtons = document.querySelectorAll('.btn-back');
    const navMyJobs = document.getElementById('nav-my-jobs');
    const reloadJobsBtn = document.getElementById('reload-jobs-btn');
    const screens = document.querySelectorAll('.screen');

    if (!jobTableBody) {
        console.error('❌ Không tìm thấy job-table-body!');
        return;
    }

    // --- Hàm thay đổi màn hình ---
    function showScreen(screenId) {
        screens.forEach(screen => screen.classList.remove('active'));
        const activeScreen = document.getElementById(screenId);
        if (activeScreen) {
            activeScreen.classList.add('active');
        }
        // Cuộn lên đầu trang
        const mainContent = document.getElementById('main-content');
        if (mainContent) mainContent.scrollTop = 0;
    }

    // --- Hàm hiển thị lỗi ---
    function showError(container, message) {
        container.innerHTML = `
            <tr><td colspan="5" class="error-placeholder">
                <i class="fa-solid fa-exclamation-triangle"></i>
                <strong>Đã xảy ra lỗi:</strong> ${message}
            </td></tr>`;
    }

    function showFormError(message) {
        const errorMsg = document.getElementById('form-error-message');
        if (errorMsg) {
            errorMsg.textContent = message;
            errorMsg.style.display = 'block';
        }
    }

    // --- HÀM TÍCH HỢP CHO MÀN HÌNH 1: Danh sách công việc ---
    async function renderJobList() {
        console.log('🔄 Đang tải danh sách công việc...');

        jobTableBody.innerHTML = `
            <tr><td colspan="5" class="loading-placeholder">
                <i class="fa-solid fa-spinner fa-spin"></i> Đang tải ...
            </td></tr>`;

        try {
            // 1. Gọi API lấy Danh sách Claim
            const pageObject = await api.get('/claims?status=APPROVED&page=0&size=50');
            const claimsList = pageObject.content;

            console.log('📦 Nhận được claims:', claimsList);

            if (!claimsList || claimsList.length === 0) {
                jobTableBody.innerHTML = `
                    <tr><td colspan="5" class="loading-placeholder">
                        <i class="fa-solid fa-check-circle"></i> Không có công việc nào ...
                    </td></tr>`;
                return;
            }

            jobTableBody.innerHTML = ''; // Xóa bảng cũ

            // 2. Lặp qua danh sách Claim
            for (const claim of claimsList) {
                let statusHtml = `<span class="status-tag status-pending"><i class="fa-solid fa-spinner fa-spin"></i> Đang kiểm tra...</span>`;
                let actionHtml = `<span style="color: var(--text-secondary); font-size: 0.875rem;">Đang kiểm tra...</span>`;

                // Tạo hàng trước, sau đó cập nhật
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td><strong>${claim.claimCode}</strong></td>
                    <td>${claim.vin}</td>
                    <td>${claim.description.substring(0, 50)}...</td>
                    <td data-status-cell-for="${claim.id}">${statusHtml}</td>
                    <td data-action-cell-for="${claim.id}">${actionHtml}</td>
                `;
                jobTableBody.appendChild(row);

                // API: GET /api/v1/allocations/status-by-claim/{claimId}
                try {
                    const partStatusDto = await api.get(`/allocations/status-by-claim/${claim.id}`);

                    // Cập nhật cell trạng thái và hành động
                    const statusCell = document.querySelector(`[data-status-cell-for="${claim.id}"]`);
                    const actionCell = document.querySelector(`[data-action-cell-for="${claim.id}"]`);

                    // Dựa trên PartAllocationStatusDto.AllocationStatus
                    if (partStatusDto.status === 'READY_TO_INSTALL' || partStatusDto.status === 'NOT_REQUIRED') {
                        statusHtml = `<span class="status-tag status-ready"><i class="fa-solid fa-check-circle"></i> Sẵn sàng</span>`;
                        actionHtml = `<a class="view-claim" data-claim-id="${claim.id}">Bắt đầu sửa</a>`;
                    } else {
                        statusHtml = `<span class="status-tag status-pending"><i class="fa-solid fa-clock"></i> Chờ phụ tùng</span>`;
                        actionHtml = `<span style="color: var(--text-secondary); font-size: 0.875rem;">Không thể bắt đầu</span>`;
                    }

                    if (statusCell) statusCell.innerHTML = statusHtml;
                    if (actionCell) actionCell.innerHTML = actionHtml;

                } catch (partError) {
                    console.error('⚠️ Lỗi kiểm tra phụ tùng:', partError);
                    const statusCell = document.querySelector(`[data-status-cell-for="${claim.id}"]`);
                    const actionCell = document.querySelector(`[data-action-cell-for="${claim.id}"]`);
                    if (statusCell) statusCell.innerHTML = `<span style="color: var(--danger-color);">Lỗi</span>`;
                    if (actionCell) actionCell.innerHTML = `Lỗi kiểm tra P.Tùng`;
                }
            }

        } catch (error) {
            console.error('❌ Lỗi tải danh sách:', error);
            showError(jobTableBody, error.message);
        }
    }

    // --- HÀM TÍCH HỢP CHO MÀN HÌNH 2: Chi tiết sửa chữa ---
    async function loadClaimDetails(claimId) {
        showScreen('screen-repair-details');
        document.getElementById('detail-loading').style.display = 'block';
        document.getElementById('detail-content').style.display = 'none';
        document.getElementById('detail-claim-id').textContent = '...';

        try {
            currentClaimData = await api.get(`/claims/${claimId}`);

            if (!currentClaimData) throw new Error("Không tìm thấy dữ liệu claim.");

            document.getElementById('detail-claim-id').textContent = currentClaimData.claimCode;
            document.getElementById('detail-vin').textContent = currentClaimData.vin;
            document.getElementById('detail-customer').textContent = currentClaimData.customerName;
            document.getElementById('detail-date').textContent = new Date(currentClaimData.dateCreated).toLocaleDateString('vi-VN');
            document.getElementById('detail-description').textContent = currentClaimData.description;

            const partsTableBody = document.getElementById('detail-parts-table').querySelector('tbody');
            partsTableBody.innerHTML = '';
            if (currentClaimData.partList && currentClaimData.partList.length > 0) {
                currentClaimData.partList.forEach(part => {
                    partsTableBody.innerHTML += `<tr>
                        <td>${part.partName}</td>
                        <td>${part.partNumber}</td>
                        <td>${part.quantity}</td>
                    </tr>`;
                });
            } else {
                partsTableBody.innerHTML = `<tr><td colspan="3" class="loading-placeholder">Không yêu cầu phụ tùng.</td></tr>`;
            }

            const procedureList = document.getElementById('detail-procedure-list');
            if (currentClaimData.prepairProcedure) {
                procedureList.innerHTML = `<ol>${currentClaimData.prepairProcedure}</ol>`;
            } else {
                procedureList.innerHTML = `<p class="text-secondary">Không có hướng dẫn sửa chữa.</p>`;
            }

            const serialInputsContainer = document.getElementById('form-serial-inputs');
            serialInputsContainer.innerHTML = '';
            if (currentClaimData.partList && currentClaimData.partList.length > 0) {
                currentClaimData.partList.forEach(part => {
                    serialInputsContainer.innerHTML += `
                        <div class="form-group">
                            <label for="serial-${part.partNumber}">${part.partName} (${part.partNumber})</label>
                            <input type="text" id="serial-${part.partNumber}" data-part-number="${part.partNumber}" class="form-control serial-input" placeholder="Quét hoặc nhập số serial phụ tùng mới" required>
                        </div>
                    `;
                });
            } else {
                serialInputsContainer.innerHTML = `<p class="text-secondary">Không có phụ tùng nào cần nhập serial.</p>`;
            }

            document.getElementById('form-claim-id').textContent = currentClaimData.claimCode;

            document.getElementById('detail-loading').style.display = 'none';
            document.getElementById('detail-content').style.display = 'block';

        } catch (error) {
            document.getElementById('detail-loading').innerHTML = `
                <i class="fa-solid fa-exclamation-triangle"></i>
                <strong>Đã xảy ra lỗi:</strong> ${error.message}`;
        }
    }

    // --- HÀM TÍCH HỢP CHO MÀN HÌNH 3: Báo cáo kết quả ---
    async function submitRepairReport(e) {
        e.preventDefault();
        btnSubmitReport.disabled = true;
        btnSubmitReport.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Đang gửi...`;
        document.getElementById('form-error-message').style.display = 'none';

        const serialUpdates = {};
        const serialInputs = document.querySelectorAll('.serial-input');
        serialInputs.forEach(input => {
            const partNumber = input.dataset.partNumber;
            const serialValue = input.value;
            serialUpdates[partNumber] = { newSerial: serialValue, oldSerial: null };
        });

        const claimRepairResultDto = {
            technicianId: currentTechnicianId,
            finalNotes: document.getElementById('repair-notes').value,
            serialUpdates: serialUpdates
        };

        const claimId = currentClaimData.claimId || currentClaimData.id;
        if (!claimId) {
                alert("Lỗi hệ thống: Không tìm thấy ID của yêu cầu bảo hành này.");
                btnSubmitReport.disabled = false;
                btnSubmitReport.innerHTML = `<i class="fa-solid fa-paper-plane"></i> Gửi Báo cáo`;
                return;
            }

        try {
            await api.put(`/claims/${claimId}/repair-result`, claimRepairResultDto);

            alert(`Đã gửi báo cáo thành công cho Claim: ${currentClaimData.claimCode}`);

            reportForm.reset();
            btnSubmitReport.innerHTML = `<i class="fa-solid fa-paper-plane"></i> Gửi Báo cáo`;
            renderJobList();
            showScreen('screen-job-list');

        } catch (error) {
            showFormError(error.message);
            btnSubmitReport.disabled = false;
            btnSubmitReport.innerHTML = `<i class="fa-solid fa-paper-plane"></i> Gửi Báo cáo`;
        }
    }
    // --- HÀM CHO MÀN HÌNH 4: LỊCH SỬ ---
async function loadHistory() {
    const tableBody = document.getElementById('history-table-body');
    const loading = document.getElementById('history-loading');
    const empty = document.getElementById('history-empty');

    // Lấy giá trị bộ lọc
    const fromDate = document.getElementById('history-from-date').value;
    const toDate = document.getElementById('history-to-date').value;
    const status = document.getElementById('history-status').value;

    showScreen('screen-history');
    tableBody.innerHTML = '';
    loading.style.display = 'block';
    empty.style.display = 'none';

    try {
        let url = '/claims?page=0&size=20&sort=dateCreated,desc';

        if (status) url += `&status=${status}`;
        if (fromDate) url += `&fromDate=${fromDate}T00:00:00`;
        if (toDate) url += `&toDate=${toDate}T23:59:59`;

        const response = await api.get(url);
        const claims = response.content || [];

        loading.style.display = 'none';

        if (claims.length === 0) {
            empty.style.display = 'block';
            return;
        }

        // Render dữ liệu ra bảng
        claims.forEach(claim => {
            // 🛠️ SỬA QUAN TRỌNG: Lấy status đúng tên biến
            // Backend có thể trả về 'status' hoặc 'currentStatus' tùy DTO
            const rawStatus = claim.status || claim.currentStatus;

            // Log để kiểm tra (bạn có thể xóa sau này)
            console.log(`Claim: ${claim.claimCode}, Status lấy được: ${rawStatus}`);

            // LỌC: Chỉ ẩn những xe đang chờ/mới/đang sửa
            // Lưu ý: Phải dùng biến 'rawStatus' vừa lấy được để so sánh
            if (rawStatus === 'WAITING_APPROVAL' || rawStatus === 'APPROVED' || rawStatus === 'IS_PROCESSING' || rawStatus === 'NEW') return;

            const row = document.createElement('tr');

            // Xử lý Badge (dùng rawStatus)
            let statusBadge = '';
            if (rawStatus === 'COMPLETED') {
                 statusBadge = '<span class="status-badge status-done">Hoàn tất</span>';
            } else if (rawStatus === 'REJECTED') {
                 statusBadge = '<span class="status-badge status-cancel">Từ chối</span>';
            } else {
                 statusBadge = `<span class="status-badge">${rawStatus}</span>`;
            }

            const createdDateStr = claim.dateCreated ? new Date(claim.dateCreated).toLocaleDateString('vi-VN') : '-';
            const modDateStr = '-';

            row.innerHTML = `
                <td><strong>${claim.claimCode}</strong></td>
                <td>
                    <div>${claim.vin}</div>
                    <div class="text-secondary" style="font-size: 12px;">${claim.vehicleModel || 'VF8'}</div>
                </td>
                <td>${createdDateStr}</td>
                <td>${statusBadge}</td>
                <td>
                    <button class="btn-icon view-history-detail" data-claim-id="${claim.id || claim.claimCode}">
                        <i class="fa-solid fa-eye"></i>
                    </button>
                </td>
            `;
            tableBody.appendChild(row);
        });

        if (tableBody.children.length === 0) {
            empty.style.display = 'block';
            empty.textContent = "Không có dữ liệu lịch sử phù hợp.";
        }

    } catch (error) {
        console.error(error);
        loading.innerHTML = `<span class="text-danger">Lỗi tải dữ liệu: ${error.message}</span>`;
    }
}
async function loadProfile() {
    showScreen('screen-profile');

    // Hiển thị loading
    document.getElementById('profile-name').textContent = "Đang tải...";

    try {
        // Gọi API: GET /api/v1/technicians/{id}
        const response = await api.get(`/technicians/${currentTechnicianId}`);

        console.log("📦 [PROFILE] API Response:", response);

        // 🛠️ XỬ LÝ DỮ LIỆU (Quan trọng):
        // Controller trả về ApiResponse, nên dữ liệu thật thường nằm trong .result hoặc .data
        // Dòng này sẽ tự động tìm đúng chỗ chứa dữ liệu
        const techData = response.result || response.data || response;

        if (!techData) {
            throw new Error("Không tìm thấy dữ liệu Technician trong phản hồi API");
        }

        // 1. Avatar & Tên hiển thị
        // Lưu ý: Kiểm tra kỹ xem DTO Java trả về 'technicianName' hay 'fullName'
        const name = techData.technicianName || techData.fullName || "Không có tên";
        document.getElementById('profile-name').textContent = name;
        document.getElementById('profile-avatar-text').textContent = name.charAt(0).toUpperCase();

        // 2. Badge Trạng thái
        const statusDiv = document.getElementById('profile-status-badge');
        // Giả sử DTO trả về status là 'ACTIVE', 'BUSY'...
        const status = techData.status || 'UNKNOWN';

        if (status === 'ACTIVE' || status === 'AVAILABLE') {
            statusDiv.innerHTML = '<span class="status-badge status-done">Đang hoạt động</span>';
        } else if (status === 'BUSY') {
            statusDiv.innerHTML = '<span class="status-badge status-processing">Đang bận</span>';
        } else {
            statusDiv.innerHTML = `<span class="status-badge status-cancel">${status}</span>`;
        }

        // 3. Điền vào các ô input
        document.getElementById('profile-id').value = techData.technicianId || currentTechnicianId;
        document.getElementById('profile-fullname').value = name;
        document.getElementById('profile-phone').value = techData.phoneNum || techData.phoneNumber || "Chưa cập nhật";
        document.getElementById('profile-level').value = techData.technicianLevel || techData.level || "KTV Cơ bản";

    } catch (error) {
        console.error("❌ Lỗi tải profile:", error);
        document.getElementById('profile-name').textContent = "Lỗi tải dữ liệu";

        // Fallback: Nếu API lỗi, hiện thông tin tạm từ token đăng nhập
        const user = getUser();
        if(user) {
             document.getElementById('profile-id').value = user.id || user.sub;
             document.getElementById('profile-fullname').value = user.username || "User";
        }

        // Hiển thị lỗi lên màn hình (tùy chọn)
        // alert("Không thể tải thông tin chi tiết: " + error.message);
    }
}


    // --- XỬ LÝ SỰ KIỆN ---

    jobTableBody.addEventListener('click', (e) => {
        if (e.target.classList.contains('view-claim')) {
            const claimId = e.target.dataset.claimId;
            loadClaimDetails(claimId);
        }
    });

    if (btnFinishRepair) {
        btnFinishRepair.addEventListener('click', () => {
            showScreen('screen-report-form');
        });
    }

    backButtons.forEach(button => {
        button.addEventListener('click', () => {
            showScreen('screen-job-list');
        });
    });

    if (navMyJobs) {
        navMyJobs.addEventListener('click', (e) => {
            e.preventDefault();
            showScreen('screen-job-list');
        });
    }

    if (reloadJobsBtn) {
        reloadJobsBtn.addEventListener('click', (e) => {
            e.preventDefault();
            renderJobList();
        });
    }

    if (confirmCheckbox && btnSubmitReport) {
        confirmCheckbox.addEventListener('change', () => {
            btnSubmitReport.disabled = !confirmCheckbox.checked;
        });
    }

    if (reportForm) {
        reportForm.addEventListener('submit', submitRepairReport);
    }
// Sự kiện click vào sidebar "Lịch sử" (Cần dùng setTimeout hoặc gọi sau khi renderSidebar xong)
    setTimeout(() => {
        const navHistory = document.getElementById('nav-history');
        const navMyJobs = document.getElementById('nav-my-jobs');
        const btnFilterHistory = document.getElementById('btn-filter-history');

        if (navHistory) {
            navHistory.addEventListener('click', (e) => {
                e.preventDefault();
                // Active class cho sidebar
                document.querySelectorAll('.sidebar-nav a').forEach(a => a.classList.remove('active'));
                navHistory.classList.add('active');

                // Load màn hình lịch sử
                loadHistory();
            });
        }

        if (navMyJobs) {
            navMyJobs.addEventListener('click', (e) => {
                e.preventDefault();
                document.querySelectorAll('.sidebar-nav a').forEach(a => a.classList.remove('active'));
                navMyJobs.classList.add('active');
                showScreen('screen-job-list');
            });
        }

        if (btnFilterHistory) {
            btnFilterHistory.addEventListener('click', () => {
                loadHistory();
            });
        }

        // Sự kiện xem chi tiết trong bảng Lịch sử
        const historyTableBody = document.getElementById('history-table-body');
        if(historyTableBody) {
            historyTableBody.addEventListener('click', (e) => {
                // Tìm nút bấm (hoặc icon bên trong nút)
                const btn = e.target.closest('.view-history-detail');
                if (btn) {
                    const claimId = btn.dataset.claimId;
                    // Tái sử dụng hàm loadClaimDetails cũ nhưng cần ẩn nút "Báo cáo" đi
                    loadClaimDetails(claimId);

                    // Ẩn nút chuyển sang màn hình báo cáo vì đây là xem lịch sử
                    setTimeout(() => {
                        const btnFinish = document.getElementById('btn-finish-repair');
                        if(btnFinish) btnFinish.style.display = 'none';
                    }, 100);
                }
            });
        }

    }, 500);
    const navProfile = document.getElementById('nav-profile');
        if (navProfile) {
            navProfile.addEventListener('click', (e) => {
                e.preventDefault();

                // Active menu
                document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
                navProfile.classList.add('active');

                loadProfile();
            });
        }

        // Sự kiện Đăng xuất
        const btnLogout = document.getElementById('btn-logout');
        if (btnLogout) {
            btnLogout.addEventListener('click', () => {
                if(confirm("Bạn có chắc chắn muốn đăng xuất?")) {
                    localStorage.clear(); // Xóa token
                    window.location.href = '/index.html'; // Quay về trang login
                }
            });
        }
    // --- Khởi chạy lần đầu ---
    renderJobList();
}
