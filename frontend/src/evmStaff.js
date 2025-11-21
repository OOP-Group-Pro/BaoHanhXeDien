// src/evmStaff.js

// 1. IMPORT
import { checkAuth } from './utils/auth.js';
import { renderHeader } from './components/Header.js';
import { renderEvmSidebar } from './components/EvmSidebar.js';
// ⬇️ Import API getClaims để lấy dữ liệu
import { getClaims } from './services/warrantyService.js';

// ⬇️ Import của bạn của bạn (Giữ nguyên đường dẫn nếu file đó tồn tại)
// Nếu chưa có file này, bạn hãy comment lại để tránh lỗi code
import { setupCampaignManagement } from './JS/evm-campaigns.js';

import { setupEvmDashboard } from './JS/evm-dashboard.js';


// 2. HÀM MAIN
document.addEventListener('DOMContentLoaded', main);

function main() {
    console.log("LOG: evmStaff main()");

    // 1. Gác cổng (Chỉ cho phép ROLE_EVM_STAFF)
    const userInfo = checkAuth('ROLE_EVM_STAFF');
    if (!userInfo) return;

    console.log("LOG: Login as EVM Staff OK.");

    // 2. Vẽ giao diện chung
    try {
        renderHeader();
        renderEvmSidebar();
    } catch (e) {
        console.warn("Lỗi render layout:", e);
    }

    // 3. Router
    const path = window.location.pathname;
    const bodyId = document.body.id;

    // --- TRANG DASHBOARD (index.html) ---
    if (path.endsWith('/evmStaff/') || path.endsWith('/evmStaff/index.html')) {
        console.log("LOG: Init Dashboard EVM...");
        setupEvmDashboard();
    }

    // --- TRANG CHIẾN DỊCH ---
    if (bodyId === 'evm-campaigns-page' || path.includes('campaigns.html')) {
        console.log("LOG: Init Campaign Management...");
        // Gọi code của bạn của bạn
        if (typeof setupCampaignManagement === 'function') {
            setupCampaignManagement();
        }
    }
}