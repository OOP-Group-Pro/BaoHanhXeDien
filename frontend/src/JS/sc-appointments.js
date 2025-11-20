// src/JS/appointments.js
import {
    searchCampaigns,
    getAppointmentsByCampaign,
    createAppointment,
    rescheduleAppointment,
    searchAffectedVehicles, // Dùng để check VIN
    createNotification
} from '../services/campaignService.js';

// Biến lưu trạng thái trang
let state = {
    campaignId: '',
    status: 'SCHEDULED', // Mặc định hiển thị các lịch sắp tới
    page: 0,
    size: 10
};

// Modal Instances (Bootstrap)
let createApptModal = null;
let rescheduleModal = null;

/**
 * Hàm khởi tạo chính (Được gọi từ scStaff.js)
 */
export function setupAppointmentsPage() {
    console.log("LOG: Đang khởi tạo trang Quản lý Lịch hẹn...");

    // 1. Khởi tạo Modal Bootstrap
    const modalApptEl = document.getElementById('createApptModal');
    if (modalApptEl) createApptModal = new bootstrap.Modal(modalApptEl);

    const modalRescheduleEl = document.getElementById('rescheduleModal');
    if (modalRescheduleEl) rescheduleModal = new bootstrap.Modal(modalRescheduleEl);

    // 2. Gán sự kiện cho các nút
    document.getElementById('btnFilter').addEventListener('click', handleFilter);
    document.getElementById('btnCheckVin').addEventListener('click', handleCheckVin);
    document.getElementById('btnSaveAppt').addEventListener('click', handleSaveNewAppointment);
    document.getElementById('btnSaveReschedule').addEventListener('click', handleSaveReschedule);

    // Khi đổi chiến dịch ở dropdown tạo mới -> reset input VIN
    document.getElementById('newApptCampaign').addEventListener('change', () => {
        document.getElementById('newApptVin').value = '';
        document.getElementById('vinCheckResult').textContent = '';
        document.getElementById('newApptAffectedId').value = '';
    });

    // 3. Tải dữ liệu ban đầu
    loadCampaigns();
}

/**
 * Tải danh sách chiến dịch vào 2 dropdown:
 * 1. Dropdown lọc ở màn hình chính (#filterCampaign)
 * 2. Dropdown chọn chiến dịch khi tạo lịch mới (#newApptCampaign)
 */
async function loadCampaigns() {
    try {
        // Lấy các chiến dịch đang ACTIVE
        const response = await searchCampaigns({ status: 'ACTIVE', size: 100 });
        const campaigns = response.content || [];

        const filterSelect = document.getElementById('filterCampaign');
        const modalSelect = document.getElementById('newApptCampaign');

        // Reset option
        filterSelect.innerHTML = '<option value="">-- Chọn Chiến dịch --</option>';
        modalSelect.innerHTML = '<option value="">-- Chọn --</option>';

        campaigns.forEach(camp => {
            const option = `<option value="${camp.id}">${camp.code} - ${camp.title}</option>`;
            filterSelect.insertAdjacentHTML('beforeend', option);
            modalSelect.insertAdjacentHTML('beforeend', option);
        });

        console.log("LOG: Đã tải xong danh sách chiến dịch.");

    } catch (error) {
        console.error("Lỗi tải chiến dịch:", error);
        alert("Không thể tải danh sách chiến dịch.");
    }
}

/**
 * Xử lý khi bấm nút "Lọc Dữ liệu"
 */
async function handleFilter() {
    const campaignId = document.getElementById('filterCampaign').value;
    const status = document.getElementById('filterStatus').value;

    if (!campaignId) {
        alert("Vui lòng chọn một chiến dịch để xem.");
        return;
    }

    // Cập nhật state
    state.campaignId = campaignId;
    state.status = status;
    state.page = 0; // Về trang đầu

    await fetchAndRenderAppointments();
}

/**
 * Tải và hiển thị bảng lịch hẹn
 */
async function fetchAndRenderAppointments() {
    const tbody = document.getElementById('appointmentsTbody');
    tbody.innerHTML = '<tr><td colspan="5" class="text-center"><div class="spinner-border text-primary"></div></td></tr>';

    try {
        const params = {
            status: state.status === 'ALL' ? '' : state.status, // Nếu chọn Tất cả thì không gửi status
            page: state.page,
            size: state.size
        };

        // Gọi API lấy danh sách lịch theo chiến dịch
        const data = await getAppointmentsByCampaign(state.campaignId, params);

        renderTable(data.content);
        renderPagination(data);

    } catch (error) {
        console.error("Lỗi tải lịch hẹn:", error);
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-danger">Lỗi: ${error.message}</td></tr>`;
    }
}

function renderTable(appointments) {
    const tbody = document.getElementById('appointmentsTbody');
    tbody.innerHTML = '';

    if (!appointments || appointments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-3">Không tìm thấy lịch hẹn nào.</td></tr>';
        return;
    }

    appointments.forEach(appt => {
        // Format ngày giờ
        const date = new Date(appt.scheduledAt).toLocaleString('vi-VN');

        // Badge trạng thái
        let badgeClass = 'bg-secondary';
        if (appt.status === 'SCHEDULED') badgeClass = 'bg-primary';
        else if (appt.status === 'DONE') badgeClass = 'bg-success';
        else if (appt.status === 'CANCELLED') badgeClass = 'bg-danger';
        else if (appt.status === 'RESCHEDULED') badgeClass = 'bg-warning text-dark';

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="ps-4 fw-bold text-primary">#${appt.appointmentId}</td>
            <td>
                 <div>VIN: <strong>${appt.vehicleVin || 'N/A'}</strong></div>
                 <small class="text-muted">KH: ${appt.customerName || '---'}</small>
            </td>
            <td><i class="bi bi-clock me-1"></i>${date}</td>
            <td><span class="badge ${badgeClass}">${appt.status}</span></td>
            <td class="text-end pe-4">
                ${appt.status === 'SCHEDULED' || appt.status === 'RESCHEDULED' ? `
                    <button class="btn btn-sm btn-outline-warning me-1 btn-reschedule"
                            data-id="${appt.appointmentId}"
                            data-time="${appt.scheduledAt}">
                        <i class="bi bi-pencil-square"></i> Đổi lịch
                    </button>
                ` : ''}

                ${appt.status === 'DONE' ? '<span class="text-success"><i class="bi bi-check-all"></i> Hoàn tất</span>' : ''}
            </td>
        `;
        tbody.appendChild(tr);
    });

    // Gắn sự kiện cho các nút trong bảng (Đổi lịch)
    document.querySelectorAll('.btn-reschedule').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = e.currentTarget.getAttribute('data-id');
            const time = e.currentTarget.getAttribute('data-time');
            openRescheduleModal(id, time);
        });
    });
}

function renderPagination(pageData) {
    const paginationUl = document.getElementById('pagination');
    paginationUl.innerHTML = '';

    const { totalPages, number: currentPage, first, last } = pageData;
    if (totalPages <= 1) return;

    // Nút Trước
    paginationUl.innerHTML += `
        <li class="page-item ${first ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage - 1}); return false;">Trước</a>
        </li>
    `;

    // Các trang số
    for (let i = 0; i < totalPages; i++) {
        paginationUl.innerHTML += `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" onclick="changePage(${i}); return false;">${i + 1}</a>
            </li>
        `;
    }

    // Nút Sau
    paginationUl.innerHTML += `
        <li class="page-item ${last ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage + 1}); return false;">Sau</a>
        </li>
    `;
}

// Hàm Global để gọi từ onclick trong HTML (phân trang)
window.changePage = (page) => {
    state.page = page;
    fetchAndRenderAppointments();
};


// ============================================================
// LOGIC: TẠO LỊCH HẸN MỚI
// ============================================================

/**
 * Kiểm tra xem VIN có thuộc chiến dịch đã chọn không?
 */
async function handleCheckVin() {
    const campaignId = document.getElementById('newApptCampaign').value;
    const vin = document.getElementById('newApptVin').value.trim();
    const resultEl = document.getElementById('vinCheckResult');
    const hiddenIdEl = document.getElementById('newApptAffectedId');

    if (!campaignId) {
        alert("Vui lòng chọn Chiến dịch trước.");
        return;
    }
    if (vin.length !== 17) {
        resultEl.textContent = "VIN phải đủ 17 ký tự.";
        return;
    }

    resultEl.className = "form-text text-muted";
    resultEl.textContent = "Đang kiểm tra...";

    try {
        // Gọi API tìm kiếm xe trong chiến dịch
        const response = await searchAffectedVehicles(campaignId, { vin: vin });

        if (response.content && response.content.length > 0) {
            const vehicle = response.content[0]; // Lấy xe đầu tiên tìm thấy

            // Kiểm tra trạng thái xe
            if (vehicle.status === 'COMPLETED') {
                resultEl.className = "form-text text-warning";
                resultEl.textContent = "Xe này đã hoàn thành chiến dịch rồi.";
                hiddenIdEl.value = "";
            } else {
                resultEl.className = "form-text text-success fw-bold";
                resultEl.innerHTML = `<i class="bi bi-check-circle"></i> Hợp lệ! (Trạng thái: ${vehicle.status})`;
                hiddenIdEl.value = vehicle.affectedId; // Lưu ID để lát gửi API
            }
        } else {
            resultEl.className = "form-text text-danger";
            resultEl.textContent = "Xe này KHÔNG nằm trong danh sách ảnh hưởng của chiến dịch.";
            hiddenIdEl.value = "";
        }

    } catch (error) {
        console.error(error);
        resultEl.className = "form-text text-danger";
        resultEl.textContent = "Lỗi kiểm tra VIN: " + error.message;
    }
}

async function handleSaveNewAppointment() {
    const campaignId = document.getElementById('newApptCampaign').value;
    const affectedId = document.getElementById('newApptAffectedId').value;
    const scheduledAt = document.getElementById('newApptDate').value;
    const serviceCenterId = document.getElementById('newApptCenterId').value;

    if (!campaignId || !affectedId || !scheduledAt) {
        alert("Vui lòng kiểm tra VIN hợp lệ và chọn ngày giờ.");
        return;
    }

    try {
        // 1. Tạo lịch hẹn
        const apptDto = {
            campaignId: campaignId,
            affectedId: affectedId,
            scheduledAt: scheduledAt,
            serviceCenterId: serviceCenterId
        };
        await createAppointment(apptDto);

        // 2. (Tùy chọn) Gửi thông báo SMS/Email xác nhận cho khách
        // Gọi API NotificationController mà chúng ta vừa thêm
        try {
            await createNotification({
                campaignId: campaignId,
                affectedId: affectedId,
                channel: "SMS", // Mặc định SMS
                status: "PENDING"
            });
            console.log("LOG: Đã tạo yêu cầu gửi thông báo.");
        } catch (e) {
            console.warn("Không thể tạo thông báo:", e);
        }

        alert("Đặt lịch thành công!");
        createApptModal.hide();

        // Reset form
        document.getElementById('createApptForm').reset();
        document.getElementById('vinCheckResult').textContent = '';

        // Reload bảng nếu đang xem đúng chiến dịch đó
        if (state.campaignId == campaignId) {
            fetchAndRenderAppointments();
        }

    } catch (error) {
        alert("Lỗi tạo lịch: " + error.message);
    }
}


// ============================================================
// LOGIC: ĐỔI LỊCH (RESCHEDULE)
// ============================================================

function openRescheduleModal(id, currentScheduledAt) {
    document.getElementById('rescheduleApptId').value = id;
    document.getElementById('rescheduleDate').value = currentScheduledAt; // Format lại nếu cần
    rescheduleModal.show();
}

async function handleSaveReschedule() {
    const id = document.getElementById('rescheduleApptId').value;
    const newDate = document.getElementById('rescheduleDate').value;

    if (!newDate) {
        alert("Vui lòng chọn thời gian mới.");
        return;
    }

    try {
        await rescheduleAppointment(id, {
            scheduledAt: newDate,
            serviceCenterId: 101 // Giữ nguyên center cũ hoặc cho chọn
        });

        alert("Đổi lịch thành công!");
        rescheduleModal.hide();
        fetchAndRenderAppointments(); // Tải lại bảng

    } catch (error) {
        alert("Lỗi đổi lịch: " + error.message);
    }
}