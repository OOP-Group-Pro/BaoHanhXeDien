import { api } from '../services/apiClient.js';
import { checkAuth } from '../utils/auth.js';
import {renderAdminSidebar} from "../components/AdminSidebar.js";

// API gọi ReportController
const getDashboardStats = () => api.get('/reports/dashboard-stats');

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Check quyền Admin
    const userInfo = checkAuth('ROLE_ADMIN');
    if (!userInfo) return;

    try {

        renderAdminSidebar();
        // 2. Gọi API
        const stats = await getDashboardStats();
        console.log("📊 Dashboard Data:", stats);

        // 3. Render Dữ liệu
        renderKPIs(stats.claimsByStatus);
        renderMonthChart(stats.claimsByMonth);
        renderStatusChart(stats.claimsByStatus);
        renderTopParts(stats.topFaultyParts);

    } catch (error) {
        console.error("Lỗi tải dashboard:", error);
        alert("Không thể tải dữ liệu báo cáo.");
    }
});

function renderKPIs(statusData) {
    // Convert List to Map cho dễ lấy
    // statusData dạng: [{label: "APPROVED", value: 10}, ...]
    const map = {};
    statusData.forEach(item => map[item.label] = item.value);

    // Điền số vào thẻ HTML
    document.getElementById('stat-pending').textContent = map['WAITING_APPROVAL'] || 0;
    document.getElementById('stat-approved').textContent = map['APPROVED'] || 0;
    document.getElementById('stat-rejected').textContent = map['REJECTED'] || 0;

    // Giả sử trạng thái đang xử lý gồm nhiều loại
    const processing = (map['IN_PROGRESS'] || 0) + (map['WAITING_PART'] || 0);
    document.getElementById('stat-processing').textContent = processing;
}

function renderMonthChart(monthData) {
    const ctx = document.getElementById('claimsMonthChart').getContext('2d');

    // Dữ liệu mẫu nếu API trả về thiếu tháng
    const labels = ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'];
    const values = new Array(12).fill(0);

    // Map dữ liệu từ API vào mảng values
    monthData.forEach(item => {
        // item.label là chuỗi tháng ("1", "2"...) hoặc format khác tùy query DB
        const monthIndex = parseInt(item.label) - 1;
        if (monthIndex >= 0 && monthIndex < 12) values[monthIndex] = item.value;
    });

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Số lượng Yêu cầu',
                data: values,
                borderColor: '#007BFF',
                backgroundColor: 'rgba(0, 123, 255, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: { responsive: true, maintainAspectRatio: false }
    });
}

function renderStatusChart(statusData) {
    const ctx = document.getElementById('claimsStatusChart').getContext('2d');

    const labels = statusData.map(item => item.label);
    const values = statusData.map(item => item.value);
    const colors = ['#ffc107', '#28a745', '#dc3545', '#6f42c1', '#17a2b8']; // Màu tương ứng

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: colors
            }]
        },
        options: { responsive: true, maintainAspectRatio: false }
    });
}

function renderTopParts(partsData) {
    const tbody = document.getElementById('top-parts-body');
    tbody.innerHTML = '';

    console.log("🔍 CẤU TRÚC DỮ LIỆU TOP PARTS:", JSON.stringify(partsData, null, 2));

    if (!partsData || partsData.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center">Chưa có dữ liệu hỏng hóc.</td></tr>';
        return;
    }

    // 1. Tính tổng để làm thanh %
    let totalFaults = 0;
    partsData.forEach(p => {
        // Dữ liệu trả về là { label: "MOTOR", value: 60 }
        totalFaults += Number(p.value || 0);
    });

    // 2. Render từng dòng
    partsData.forEach(p => {
        // Mapping dữ liệu từ ReportDataDto
        const code = p.label || 'N/A'; // Dùng label làm Mã Loại (Type)
        const name = p.label || 'N/A'; // Tạm dùng label làm Tên luôn (vì API không trả về tên đầy đủ)
        const qty = p.value || 0;

        const percent = totalFaults ? ((qty / totalFaults) * 100).toFixed(1) : 0;

        tbody.innerHTML += `
            <tr>
                <td><span class="badge bg-light text-dark border">${code}</span></td>
                <td>${name}</td>
                <td class="fw-bold text-danger">${qty}</td>
                <td>
                    <div class="progress" style="height: 6px; width: 100px;">
                        <div class="progress-bar bg-danger" style="width: ${percent}%"></div>
                    </div>
                    <small class="text-muted">${percent}%</small>
                </td>
            </tr>
        `;
    });
}