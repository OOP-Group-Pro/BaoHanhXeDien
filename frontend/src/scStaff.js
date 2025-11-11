// src/scStaff.js

// Import các "linh kiện" và "tiện ích"
import { checkAuth } from './utils/auth.js';
import { renderHeader } from './components/Header.js';
import { renderStaffSidebar } from './components/StaffSidebar.js';
import { getClaims, createClaim } from './services/warrantyService.js';
import { logout } from './services/authService.js'; // (Bạn cần tạo file authService.js)

// --- CHẠY CHUNG CHO MỌI TRANG STAFF ---

// 1. GÁC CỔNG: Kiểm tra xem có phải SC Staff không
const userInfo = checkAuth('ROLE_SC_STAFF');
if (!userInfo) return; // Dừng thực thi nếu không phải

// 2. VẼ GIAO DIỆN CHUNG
renderHeader();
renderStaffSidebar();

// --- LOGIC CHO TỪNG TRANG CỤ THỂ ---
// (Kiểm tra xem file HTML nào đang gọi file JS này)

// 3. Logic cho trang Dashboard (index.html)
if (window.location.pathname.endsWith('/staff/index.html')) {
    loadDashboardData();
}

// 4. Logic cho trang "Tạo Claim"
if (window.location.pathname.endsWith('/staff/create-claim.html')) {
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
            tbody.innerHTML += `
                <tr>
                    <td>${claim.claimCode}</td>
                    <td>${claim.vin}</td>
                    <td>${claim.description}</td>
                    <td><span class="status-${claim.currentStatus.toLowerCase()}">${claim.currentStatus}</span></td>
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
function setupCreateClaimForm() {
    const form = document.getElementById('claim-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault(); // Ngăn trình duyệt reload
        const button = e.target.querySelector('button[type="submit"]');
        button.disabled = true; // Chống click 2 lần
        button.textContent = 'Đang gửi...';

        const vin = document.getElementById('vin').value;
        const description = document.getElementById('description').value;

        // Tạm thời hard-code, sau này bạn sẽ làm UI phức tạp hơn
        const requestedParts = [{ partNumber: "PN-DEMO", partName: "Demo Part", quantity: 1 }];

        try {
            // Gọi API (chúng ta đã test thành công trên Postman!)
            const newClaimId = await createClaim({ vin, description, requestedParts });

            alert('Tạo Claim thành công! ID mới là: ' + newClaimId);
            window.location.href = '/pages/staff/index.html'; // Chuyển về trang danh sách

        } catch (error) {
            alert('Lỗi tạo claim: ' + error.message);
            button.disabled = false;
            button.textContent = 'Gửi';
        }
    });
}