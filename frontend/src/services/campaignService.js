// src/services/campaignService.js
import { api } from './apiClient.js';

// ============================================================
// 1. CAMPAIGN (Chiến dịch)
// ============================================================

/**
 * Lấy danh sách chiến dịch (để hiển thị Dropdown lọc lịch hẹn)
 * API: GET /api/v1/campaigns?status=ACTIVE
 */
export const searchCampaigns = (params = {}) => {
    // params: { code, status, type, page, size }
    const queryString = new URLSearchParams(params).toString();
    return api.get(`/campaigns?${queryString}`);
};

/**
 * Lấy chi tiết chiến dịch
 * API: GET /api/v1/campaigns/{id}
 */
export const getCampaignDetail = (id) => {
    return api.get(`/campaigns/${id}`);
};

/**
 * Tạo chiến dịch mới
 * API: POST /api/v1/campaigns
 * Body: CampaignCreateRequest (code, title, type, startAt, endAt)
 */
export const createCampaign = (dto) => {
    return api.post('/campaigns', dto);
};

/**
 * Cập nhật chiến dịch
 * API: PUT /api/v1/campaigns/{id}
 */
export const updateCampaign = (id, dto) => {
    return api.put(`/campaigns/${id}`, dto);
};

/**
 * Thêm danh sách xe vào chiến dịch (Import Excel/CSV logic sẽ xử lý ở JS để gọi API này)
 * API: POST /api/v1/campaigns/{id}/affected-vehicles
 * Body: AffectedVehicleCreateRequest
 */
export const addAffectedVehicle = (campaignId, dto) => {
    return api.post(`/campaigns/${campaignId}/affected-vehicles`, dto);
};

// ============================================================
// 2. APPOINTMENT (Lịch hẹn)
// ============================================================

/**
 * Tạo lịch hẹn mới
 * API: POST /api/v1/appointments
 * Body: { campaignId, affectedId, scheduledAt, serviceCenterId }
 */
export const createAppointment = (dto) => {
    return api.post('/appointments', dto);
};

/**
 * Lấy danh sách lịch hẹn theo Chiến dịch
 * API: GET /api/v1/appointments/by-campaign/{campaignId}?status=...
 */
export const getAppointmentsByCampaign = (campaignId, params = {}) => {
    // params: { status, page, size }
    const queryString = new URLSearchParams(params).toString();
    return api.get(`/appointments/by-campaign/${campaignId}?${queryString}`);
};

/**
 * Lấy danh sách lịch hẹn theo Xe bị ảnh hưởng (AffectedVehicle)
 * API: GET /api/v1/appointments/by-affected/{affectedId}
 */
export const getAppointmentsByAffected = (affectedId, params = {}) => {
    const queryString = new URLSearchParams(params).toString();
    return api.get(`/appointments/by-affected/${affectedId}?${queryString}`);
};

/**
 * Dời lịch hẹn (Reschedule)
 * API: POST /api/v1/appointments/{id}/reschedule
 * Body: { scheduledAt, serviceCenterId }
 */
export const rescheduleAppointment = (id, dto) => {
    return api.post(`/appointments/${id}/reschedule`, dto);
};

/**
 * Hoàn thành lịch hẹn (Complete)
 * API: POST /api/v1/appointments/{id}/complete
 * Body: { outcome }
 */
export const completeAppointment = (id, dto) => {
    return api.post(`/appointments/${id}/complete`, dto);
};

/**
 * Xóa/Hủy lịch hẹn
 * API: DELETE /api/v1/appointments/{id}
 */
export const deleteAppointment = (id) => {
    return api.delete(`/appointments/${id}`);
};


// ============================================================
// 3. AFFECTED VEHICLE (Xe trong diện ảnh hưởng)
// ============================================================

/**
 * Tìm kiếm xe trong chiến dịch (để đặt lịch cho xe chưa có lịch)
 * API: GET /api/v1/campaigns/{campaignId}/affected-vehicles?vin=...&status=...
 */
export const searchAffectedVehicles = (campaignId, params = {}) => {
    const queryString = new URLSearchParams(params).toString();
    return api.get(`/campaigns/${campaignId}/affected-vehicles?${queryString}`);
};

/**
 * Đặt lịch nhanh từ phía AffectedVehicle (Optional)
 * API: POST /api/v1/campaigns/{campaignId}/affected-vehicles/{affectedId}/schedule
 */
export const scheduleAffectedVehicle = (campaignId, affectedId, dto) => {
    return api.post(`/campaigns/${campaignId}/affected-vehicles/${affectedId}/schedule`, dto);
};


// ============================================================
// 4. NOTIFICATION (Thông báo) - Tích hợp
// ============================================================

/**
 * Tạo thông báo (Gửi SMS/Email cho khách)
 * API: POST /api/v1/notifications
 * Body: { campaignId, affectedId, channel, status }
 */
export const createNotification = (dto) => {
    return api.post('/notifications', dto);
};

/**
 * Lấy danh sách thông báo của một xe
 * API: GET /api/v1/notifications/by-affected/{affectedId}
 */
export const getNotificationsByAffected = (affectedId) => {
    return api.get(`/notifications/by-affected/${affectedId}`);
};