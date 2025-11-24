// src/js/technician.js

// Import các "linh kiện" và "tiện ích"
import { checkAuth } from '../utils/auth.js';
import { renderHeader } from '../components/Header.js';
import { renderTechnicianSidebar } from '../components/TechnicianSidebar.js';
import { getUser } from "../utils/storage.js";
import { api } from '../services/apiClient.js';
import '../styles/dashboard.css';

// --- BIẾN TRẠNG THÁI ---
let currentClaimData = null;
let currentTechnicianId = null;
let claimModalInstance = null;

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
                const claimIdentifier = claim.claimCode;
                if (!claimIdentifier) continue;
                const statusHtml = `<span class="status-tag status-ready"><i class="fa-solid fa-check-circle"></i> Sẵn sàng</span>`;

                            const actionHtml = `
                                <button class="btn btn-sm btn-outline-secondary btn-view-modal-details" data-id="${claimIdentifier}">
                                    <i class="fa-solid fa-eye"></i> Chi tiết
                                </button>
                                <a class="btn btn-sm btn-dark view-claim" data-claim-id="${claimIdentifier}">
                                    <i class="fa-solid fa-play"></i> Bắt đầu sửa
                                </a>
                            `;

                            // Tạo hàng và chèn dữ liệu trực tiếp
                            const row = document.createElement('tr');
                            row.innerHTML = `
                                <td><strong>${claim.claimCode}</strong></td>
                                <td>${claim.vin}</td>
                                <td>${claim.description ? claim.description.substring(0, 50) + '...' : ''}</td>
                                <td>${statusHtml}</td>
                                <td>${actionHtml}</td>
                            `;
                            jobTableBody.appendChild(row);
                        }

                    } catch (error) {
                        console.error('❌ Lỗi tải danh sách:', error);
                        showError(jobTableBody, error.message);
                    }
    }

    // --- HÀM TÍCH HỢP CHO MÀN HÌNH 2: Chi tiết sửa chữa ---
    async function loadClaimDetails(claimId) {
        if (!claimId) {
            console.error("Lỗi gọi loadClaimDetails: Claim ID bị thiếu.");
            return;
        }
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
    async function getClaimDetails(claimId) {
        // API lấy chi tiết Claim
        return api.get(`/claims/${claimId}`);
    }

    /**
     * Lấy lịch sử trạng thái (dùng trong Modal)
     */
    async function getClaimHistory(claimId) {
        // API lấy lịch sử trạng thái
        return api.get(`/claims/${claimId}/status-history`);
    }
    window.downloadFile = async (url, fileName) => {
        try {
            const fullUrl = `${api.getBaseUrl()}${url}`; // Giả định api có getBaseUrl()
            window.open(fullUrl, '_blank');
        } catch (error) {
            console.error("Lỗi tải/xem tài liệu:", error);
            alert("Không thể tải/xem file. Vui lòng thử lại.");
        }
    };

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

            const rawStatus = claim.status || claim.currentStatus;

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
        const link = e.target.closest('.view-claim');
        if (link) {
            const claimId = link.dataset.claimId;
            if (claimId) {
                loadClaimDetails(claimId);
            } else {
                console.error("Lỗi: Không tìm thấy Claim ID hợp lệ.");
            }
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

        if (historyTableBody) {
            historyTableBody.addEventListener('click', async (e) => {
                // Tìm nút xem chi tiết (hoặc icon bên trong)
                const viewBtn = e.target.closest('.view-history-detail');
                if (viewBtn) {
                    e.preventDefault();
                    const claimIdentifier = viewBtn.dataset.claimId;
                    if (claimIdentifier) {
                        console.log("Xem chi tiết lịch sử cho:", claimIdentifier);
                        // Gọi hàm mở Modal chung
                        await openClaimModal(claimIdentifier);
                    }
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

        function initClaimDetailsModal() {
            const tableBody = document.getElementById('job-table-body'); // ID của bảng Technician
            const modalEl = document.getElementById('claimDetailsModal');

            if (!tableBody || !modalEl) return;

            if (typeof bootstrap === 'undefined') {
                console.error('Bootstrap JS chưa được tải!');
                return;
            }

            // Khởi tạo Bootstrap Modal
            window.claimModalInstance = new bootstrap.Modal(modalEl);

            // Lắng nghe sự kiện click
            tableBody.addEventListener('click', async (e) => {
                const viewButton = e.target.closest('.btn-view-modal-details');
                if (viewButton) {
                    e.preventDefault();
                    const claimCode = viewButton.dataset.id;
                    await openClaimModal(claimCode);
                }
            });
        }

        // --- 2. MỞ MODAL & TẢI DỮ LIỆU (Có Lịch sử) ---
        async function openClaimModal(claimCode) {
            const modalTitle = document.getElementById('claimModalTitle');
            const modalBody = document.getElementById('claimModalBody');

            modalTitle.textContent = `Chi tiết Claim: ${claimCode}`;
            // Hiện Loading
            modalBody.innerHTML = '<div class="text-center p-5"><div class="spinner-border text-primary"></div><p class="mt-2">Đang tải dữ liệu...</p></div>';

            window.claimModalInstance.show();

            try {
                // Gọi song song 2 API: Chi tiết và Lịch sử (Giống SC Staff)
                const [details, history] = await Promise.all([
                    api.get(`/claims/${claimCode}`),
                    api.get(`/claims/${claimCode}/history`) // Đảm bảo endpoint này đúng
                ]);

                renderModalContent(details, history.content);

            } catch (error) {
                console.error("Lỗi tải modal:", error);
                modalBody.innerHTML = `<div class="alert alert-danger m-3">Lỗi tải dữ liệu: ${error.message}</div>`;
            }
        }

        // --- 3. RENDER NỘI DUNG (STYLE TABS CỦA SC STAFF) ---
        function renderModalContent(details, history) {
            const modalBody = document.getElementById('claimModalBody');

            // 1. Xác định trạng thái và màu sắc
            const statusClass = details.currentStatus === 'APPROVED' ? 'badge bg-success' : 'badge bg-info text-dark';

            // 2. TÌM "MÔ TẢ CÔNG VIỆC" TỪ KTV (Logic mới)
            // Tìm log có trạng thái COMPLETED hoặc REPAIRED để lấy ghi chú kết quả
            let technicianResult = "Chưa có báo cáo kết quả.";

            if (history && history.length > 0) {
                // Tìm log mới nhất có trạng thái hoàn thành
                const completionLog = history
                    .filter(log => log.status === 'COMPLETED' || log.status === 'REPAIRED')
                    .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))[0]; // Lấy cái mới nhất

                if (completionLog && completionLog.notes) {
                    technicianResult = completionLog.notes;
                }
            }

            // 3. Tạo HTML cho các Tab con (Phụ tùng, Lịch sử, Tài liệu)

            // A. Phụ tùng (Có hiển thị Serial mới nếu có)
            let partsHtml = '';
            if (!details.partList || details.partList.length === 0) {
                partsHtml = '<li class="list-group-item text-muted">Không có phụ tùng nào được yêu cầu.</li>';
            } else {
                partsHtml = details.partList.map(part => `
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div>
                            <strong>${part.partName || part.partNumber}</strong>
                            <div class="text-muted small">Mã: ${part.partNumber}</div>
                            ${part.serialNumberReplace ? `<div class="text-success small"><i class="fa-solid fa-barcode"></i> Serial mới: ${part.serialNumberReplace}</div>` : ''}
                        </div>
                        <span class="badge bg-primary rounded-pill">SL: ${part.quantity}</span>
                    </li>
                `).join('');
            }

            // B. Lịch sử
            let historyHtml = (history || []).map(log => `
                <li class="list-group-item">
                    <div class="d-flex justify-content-between">
                        <strong>${log.status}</strong>
                        <small class="text-muted">${new Date(log.timestamp).toLocaleString('vi-VN')}</small>
                    </div>
                    <p class="mb-0 small mt-1 text-secondary">${log.notes || ''}</p>
                    <small class="text-muted fst-italic" style="font-size: 0.75rem">Bởi: ${log.processorName || 'Hệ thống'}</small>
                </li>
            `).join('') || '<li class="list-group-item text-muted">Chưa có lịch sử.</li>';

            // C. Tài liệu
            let documentsHtml = (details.documents || []).map(doc => `
                <li class="list-group-item d-flex justify-content-between align-items-center">
                    <div class="text-truncate" style="max-width: 200px;">
                        <i class="fa-solid fa-file"></i> ${doc.fileName}
                    </div>
                    <button class="btn btn-sm btn-outline-primary" onclick="window.downloadFile('${doc.url}', '${doc.fileName}')">
                        <i class="fa-solid fa-download"></i> Tải
                    </button>
                </li>
            `).join('') || '<li class="list-group-item text-muted">Không có tài liệu.</li>';

            // 4. RENDER HTML CUỐI CÙNG (Giao diện 2 cột cho Tab Thông tin)
            modalBody.innerHTML = `
                <ul class="nav nav-tabs px-3 pt-3" id="claimTab" role="tablist">
                    <li class="nav-item"><button class="nav-link active" data-bs-toggle="tab" data-bs-target="#info-tab-pane">Thông tin & Kết quả</button></li>
                    <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#parts-tab-pane">Phụ tùng (${details.partList ? details.partList.length : 0})</button></li>
                    <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#history-tab-pane">Lịch sử</button></li>
                    <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#docs-tab-pane">Tài liệu</button></li>
                </ul>

                <div class="tab-content p-4">
                    <div class="tab-pane fade show active" id="info-tab-pane">
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <p><strong>Mã Claim:</strong> ${details.claimCode}</p>
                                <p><strong>Trạng thái:</strong> <span class="${statusClass}">${details.currentStatus}</span></p>
                                <p><strong>Số VIN:</strong> ${details.vin}</p>
                            </div>
                            <div class="col-md-6">
                                <p><strong>Khách hàng:</strong> ${details.customerName || '---'}</p>
                                <p><strong>Ngày tạo:</strong> ${new Date(details.dateCreated).toLocaleDateString('vi-VN')}</p>
                                <p><strong>KTV thực hiện:</strong> ${details.technicalName || 'Chưa gán'}</p>
                            </div>
                        </div>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="fw-bold mb-2 text-secondary"><i class="fa-solid fa-circle-exclamation"></i> Mô tả lỗi (Khách hàng):</label>
                                <div class="bg-light p-3 rounded border" style="height: 100%; min-height: 120px;">
                                    ${details.description}
                                </div>
                            </div>

                            <div class="col-md-6">
                                <label class="fw-bold mb-2 text-success"><i class="fa-solid fa-screwdriver-wrench"></i> Kết quả xử lý (KTV):</label>
                                <div class="bg-white p-3 rounded border border-success" style="height: 100%; min-height: 120px;">
                                    ${technicianResult}
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="tab-pane fade" id="parts-tab-pane"><ul class="list-group list-group-flush">${partsHtml}</ul></div>
                    <div class="tab-pane fade" id="history-tab-pane"><ul class="list-group list-group-flush">${historyHtml}</ul></div>
                    <div class="tab-pane fade" id="docs-tab-pane"><ul class="list-group list-group-flush">${documentsHtml}</ul></div>
                </div>
            `;
        }
    // --- Khởi chạy lần đầu ---
    renderJobList();
    initClaimDetailsModal();
}