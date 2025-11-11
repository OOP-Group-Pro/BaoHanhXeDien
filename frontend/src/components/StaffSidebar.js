// src/components/StaffSidebar.js
export function renderStaffSidebar() {
    const placeholder = document.getElementById('sidebar-placeholder');
    if (!placeholder) return;

    placeholder.innerHTML = `
        <aside class="sidebar">
            <nav>
                <ul>
                    <li>
                        <a href="../../pages/scStaff/index.html">Dashboard & Claims</a>
                    </li>
                    <li>
                        <a href="../../pages/scStaff/create-claim.html">Tạo Claim Mới</a>
                    </li>
                    </ul>
            </nav>
        </aside>
    `;
}
