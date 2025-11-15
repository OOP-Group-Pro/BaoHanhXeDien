// ===================== /src/evmStaff.js =====================
// Auth + UI chung
import { checkAuth } from './utils/auth.js';
import { renderHeader } from './components/Header.js';
import { renderEvmSidebar } from './components/EvmSidebar.js';

// NOTE: Không import fetchOverview/fetchMonthlyCosts ở đây
// vì dashboard module sẽ tự lo. Tránh import thừa sinh cảnh báo.

// ------------- BOOTSTRAP -------------
document.addEventListener('DOMContentLoaded', () => {
  // 1) Chỉ cho ROLE_EVM_STAFF
  const user = checkAuth('ROLE_EVM_STAFF');
  if (!user) return;

  // 2) Header + Sidebar
  try {
    renderHeader('header-placeholder');
    renderEvmSidebar('sidebar-placeholder');
  } catch (e) {
    console.warn('Header/EvmSidebar chưa sẵn sàng, dùng fallback tạm:', e);
    const h = document.getElementById('header-placeholder');
    if (h) {
      h.innerHTML = `
        <nav class="navbar navbar-light bg-white p-3 shadow-sm">
          <span class="navbar-brand mb-0 h1">EV Warranty</span>
          <span class="text-dark">Xin chào, ${user?.username || 'EVM Staff'}!</span>
        </nav>`;
    }
  }

  // 3) Router theo body id
  const id = document.body.id;

  // 3.1 Dashboard EVM
  if (id === 'evm-dashboard-page') {
    // Lazy-load module. Hỗ trợ cả 2 kiểu:
    // - module export hàm initEvmDashboardPage()
    // - hoặc module tự "auto-run" bằng side-effect
    import('./JS/evm-dashboard.js')
      .then((m) => m?.initEvmDashboardPage?.())
      .catch(console.error);
    return;
  }

  // 3.2 Quản lý Chiến dịch
  if (id === 'evm-campaigns-page') {
    import('./JS/evm-campaigns.js')
      .then(({ initEvmCampaignsPage }) => initEvmCampaignsPage?.())
      .catch(console.error);
    return;
  }

  // 3.3 Có thể bổ sung route khác tại đây:
  // if (id === 'evm-inventory-page') { ... }
  // if (id === 'evm-parts-page') { ... }
});
