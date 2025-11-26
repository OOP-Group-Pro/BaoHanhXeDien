import { getUser } from './storage.js';
import { renderAdminSidebar } from '../components/AdminSidebar.js';
import { renderTechnicianSidebar } from '../components/TechnicianSidebar.js';
import { renderStaffSidebar } from '../components/StaffSidebar.js';

export function renderSidebarByRole() {
    const user = getUser();
    console.log("DEBUG: User info from storage:", user);

    if (!user || !user.roles) {
        console.warn("❌ Không tìm thấy thông tin user hoặc roles để render sidebar");
        return;
    }

    // --- HÀM KIỂM TRA ROLE ĐA NĂNG ---
    // Chấp nhận cả trường hợp role là String ("ROLE_ADMIN") hoặc Object ({roleName: "ROLE_ADMIN"})
    const hasRole = (targetRole) => {
        // Đảm bảo user.roles là mảng
        const roles = Array.isArray(user.roles) ? user.roles : [user.roles];

        return roles.some(r => {
            // Nếu r là chuỗi -> so sánh trực tiếp
            if (typeof r === 'string') {
                return r === targetRole;
            }
            // Nếu r là object -> so sánh roleName
            if (typeof r === 'object' && r.roleName) {
                return r.roleName === targetRole;
            }
            return false;
        });
    };

    // --- LOGIC CHỌN SIDEBAR ---
    if (hasRole('ROLE_ADMIN')) {
        console.log("✅ Render Admin Sidebar");
        renderAdminSidebar();
    }
    else if (hasRole('ROLE_SC_TECHNICIAN')) {
        console.log("✅ Render Technician Sidebar");
        renderTechnicianSidebar();
    }
    else if (hasRole('ROLE_SC_STAFF')) {
        console.log("✅ Render SC Staff Sidebar");
        renderStaffSidebar();
    }
    else if (hasRole('ROLE_MANAGER')) {
        console.log("✅ Render Manager Sidebar (dùng chung Admin)");
        renderAdminSidebar();
    }
    else {
        console.error("⚠️ Không tìm thấy Sidebar phù hợp cho Roles:", user.roles);
    }
}