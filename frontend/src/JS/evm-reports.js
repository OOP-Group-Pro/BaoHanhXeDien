import { getReportStats } from '../services/reportService.js';

export function setupEvmReports() {
    console.log("LOG: Init Reports Logic...");
    loadAndRenderCharts();
}

async function loadAndRenderCharts() {
    try {
        const data = await getReportStats();

        // 1. Vẽ Biểu đồ Trạng thái (Pie)
        renderStatusChart(data.claimsByStatus);

        // 2. Vẽ Biểu đồ Top Phụ tùng (Bar)
        renderTopPartsChart(data.topFaultyParts);

        // 3. Vẽ Biểu đồ Xu hướng (Line)
        renderTrendChart(data.claimsByMonth);

    } catch (error) {
        console.error("Lỗi tải báo cáo:", error);
        alert("Không thể tải dữ liệu báo cáo.");
    }
}

function renderStatusChart(data) {
    const ctx = document.getElementById('statusChart');
    if(!ctx) return;

    const labels = data.map(d => d.label);
    const values = data.map(d => d.value);

    // Màu sắc cho các trạng thái (Mapping cứng để đẹp)
    const colors = labels.map(label => {
        if(label === 'APPROVED') return '#10B981'; // Green
        if(label === 'REJECTED') return '#EF4444'; // Red
        if(label === 'WAITING_APPROVAL') return '#F59E0B'; // Yellow
        return '#6B7280'; // Gray
    });

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: colors,
                borderWidth: 1
            }]
        },
        options: { responsive: true, maintainAspectRatio: false }
    });
}

function renderTopPartsChart(data) {
    const ctx = document.getElementById('topPartsChart');
    if(!ctx) return;

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.map(d => d.label), // Tên/Mã phụ tùng
            datasets: [{
                label: 'Số lượng hỏng',
                data: data.map(d => d.value),
                backgroundColor: '#3B82F6',
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: { y: { beginAtZero: true } }
        }
    });
}

function renderTrendChart(data) {
    const ctx = document.getElementById('trendChart');
    if(!ctx) return;

    // Tạo mảng 12 tháng (Mặc định 0 nếu không có dữ liệu)
    const months = ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'];
    const values = new Array(12).fill(0);

    data.forEach(d => {
        const monthIndex = parseInt(d.label) - 1; // data trả về tháng 1-12
        if(monthIndex >= 0 && monthIndex < 12) {
            values[monthIndex] = d.value;
        }
    });

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: months,
            datasets: [{
                label: 'Số lượng Yêu cầu',
                data: values,
                borderColor: '#8B5CF6', // Purple
                backgroundColor: 'rgba(139, 92, 246, 0.1)',
                fill: true,
                tension: 0.3 // Đường cong mềm mại
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
        }
    });
}