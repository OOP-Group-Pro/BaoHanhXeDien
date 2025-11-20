// src/evmStaff.js

// 1. IMPORT
import { checkAuth } from './utils/auth.js';
import { renderHeader } from './components/Header.js';
import { renderEvmSidebar } from './components/EvmSidebar.js';
import { setupCampaignManagement } from './JS/evm-campaigns.js';
// Import các file logic con (Sẽ tạo sau)


// 2. HÀM MAIN
document.addEventListener('DOMContentLoaded', main);

function main() {
    console.log("LOG: evmStaff main()");

    // 1. Gác cổng (Chỉ cho phép ROLE_EVM_STAFF)
    const userInfo = checkAuth('ROLE_EVM_STAFF');
    if (!userInfo) {
        console.log("LOG: Gác cổng thất bại.");
        return;
    }
    console.log("LOG: Login as EVM Staff OK.");

    // 2. Vẽ giao diện chung
    try {
        renderHeader();
        renderEvmSidebar();
    } catch (e) {
        console.warn("Lỗi render layout:", e);
    }

    // 3. Router (Điều hướng logic theo trang)
    const path = window.location.pathname;
    const bodyId = document.body.id;
    console.log("LOG: Path:", path, "| BodyID:", bodyId);

    // --- TRANG DASHBOARD (index.html) ---
    if (path.endsWith('/evmStaff/') || path.endsWith('/evmStaff/index.html')) {
        console.log("LOG: Init Dashboard EVM...");
        // initEvmDashboard(); // (Sẽ làm sau)
    }

    // --- TRANG QUẢN LÝ CHIẾN DỊCH (campaigns.html) ---
    // Chúng ta sẽ làm trang này NGAY SAU ĐÂY
    if (bodyId === 'evm-campaigns-page' || path.includes('campaigns.html')) {
        console.log("LOG: Init Campaign Management...");


        // Sau khi bạn tạo file logic JS cho trang này,
        // bạn sẽ import và gọi hàm setup ở đây. Ví dụ:
        setupCampaignManagement();
    }

}