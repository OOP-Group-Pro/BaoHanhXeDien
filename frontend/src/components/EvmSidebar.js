// src/components/EvmSidebar.js
export function renderEvmSidebar(active = 'Dashboard') {
  const html = `
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="avatar sm"></div>
        <div>
          <div class="bold">EVM Staff</div>
          <div class="muted xs">Service Center</div>
        </div>
      </div>
      <nav class="sidebar-nav">
        <a class="${active==='Dashboard'?'active':''}" href="/frontend/pages/evmStaff/dashboard.html">Dashboard</a>
        <a class="${active==='Claims'?'active':''}" href="/frontend/pages/evmStaff/claims.html">Claims</a>
        <a class="${active==='Parts'?'active':''}" href="/frontend/pages/evmStaff/parts.html">Parts</a>
        <a class="${active==='Inventory'?'active':''}" href="/frontend/pages/evmStaff/inventory.html">Inventory</a>
        <a class="${active==='Users'?'active':''}" href="/frontend/pages/evmStaff/users.html">Users</a>
      </nav>
    </aside>
  `;
  // nếu bạn có layout cố định, chèn vào vị trí #sidebar
  const mount = document.getElementById('sidebar');
  if (mount) mount.innerHTML = html;
}
