// src/components/StaffSidebar.js
export function renderStaffSidebar() {
    const placeholder = document.getElementById('sidebar-placeholder');
    if (!placeholder) return;

    // ⬇️ SỬA LẠI ĐƯỜNG DẪN ⬇️
    placeholder.innerHTML = `
        <aside class="sidebar">
            <nav class="nav flex-column">
                <h5 class="px-3 mt-2 mb-1 text-muted">SC STAFF</h5>
                <ul class="nav flex-column">
                    <li class="nav-item">
                        <a class="nav-link active" href="/pages/staff/index.html">
                            <i class="bi bi-grid-fill"></i> Dashboard & Claims
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/pages/staff/create-claim.html">
                            <i class="bi bi-plus-circle-fill"></i> Tạo Claim Mới
                        </a>
                    </li>
                </ul>
            </nav>
        </aside>
    `;
}
