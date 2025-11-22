import { api } from './apiClient.js';

/**
 * Lấy dữ liệu thống kê tổng hợp
 * API: GET /api/v1/reports/dashboard-stats
 */
export const getReportStats = () => {
    return api.get('/reports/dashboard-stats');
};