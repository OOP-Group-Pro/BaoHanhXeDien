// src/components/StaffSidebar.js

export function renderStaffSidebar() {
    const placeholder = document.getElementById('sidebar-placeholder');
    if (!placeholder) return;

    // Lấy tên file HTML hiện tại (ví dụ: 'claim-list.html' hoặc 'create-claim.html')
    const currentPath = window.location.pathname;
    const currentPage = currentPath.substring(currentPath.lastIndexOf('/') + 1);

    // Định nghĩa các biến class active
    const dashboardActive = (currentPage === 'index.html' || currentPage === 'dashboard.html' || currentPage === 'claim-list.html') ? ' active' : '';
    const createClaimActive = (currentPage === 'create-claim.html') ? ' active' : '';

    placeholder.innerHTML = `
        <aside class="sidebar">
            <nav class="nav flex-column">
                <h5 class="px-3 mt-2 mb-1 text-muted">SC STAFF</h5>
                <ul class="nav flex-column">
                    <li class="nav-item">
                        <a class="nav-link${dashboardActive}" href="index.html">
                            <i class="bi bi-grid-fill"></i> Dashboard & Claims
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link${createClaimActive}" href="create-claim.html">
                            <i class="bi bi-plus-circle-fill"></i> Tạo Claim Mới
                        </a>
                    </li>
                </ul>
            </nav>
        </aside>
    `;
}