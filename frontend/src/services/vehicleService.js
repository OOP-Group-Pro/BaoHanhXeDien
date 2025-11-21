// src/services/vehicleService.js
import { api } from './apiClient.js';

/**
 * Gọi API (của VehicleController) để lấy Tên Khách hàng bằng VIN
 * API: GET /api/v1/vehicles/{vin}/customer-name
 * (Lưu ý: API này trả về Text (String), không phải JSON)
 */
export const getCustomerNameByVin = (vin) => {
    // apiClient của chúng ta đã được sửa để xử lý text/plain
    return api.get(`/vehicles/${vin}/customer-name`);
};

/**
 * Gọi API (của VehicleController) để xác thực VIN
 * API: GET /api/v1/vehicles/validate/{vin}
 */
export const validateVin = (vin) => {
    return api.get(`/vehicles/validate/${vin}`); // API này trả về boolean
};

/**
 * Lấy chi tiết xe bằng VIN (Dùng cho bước nâng cao sau)
 * API: GET /api/v1/vehicles/vin/{vin}
 */
export const getVehicleByVin = (vin) => {
    return api.get(`/vehicles/vin/${vin}`); // API này trả về VehicleResponseDTO
};


// 2. Lấy lịch sử sửa chữa (Service History)
export const getHistoryByVehicleId = (vehicleId) => {
    // Backend trả về: List<ServiceHistoryResponseDTO> (có odometerReading)
    return api.get(`/vehicles/${vehicleId}/history`);
};

// 3. Lấy danh sách phụ tùng đang lắp (Installed Parts)
export const getPartsByVehicleId = (vehicleId) => {
    // Backend trả về: List<InstalledPartResponseDTO> (có partNumber, serialNumber)
    return api.get(`/vehicles/${vehicleId}/parts`);
};

// API: GET /api/v1/history/{id}
export const getServiceHistoryDetail = (historyId) => {
    return api.get(`/history/${historyId}`);
};

/**
 * ⬇️ THÊM MỚI: Tìm kiếm xe (Có phân trang)
 * API: GET /api/v1/vehicles?keyword=...&page=...&size=...
 */
export const searchVehicles = (keyword = '', page = 0, size = 10) => {
    const params = new URLSearchParams({
        keyword: keyword,
        page: page,
        size: size
    });
    // Lưu ý: API Backend của bạn trả về: { status, message, data: PageObject }
    // Nên chúng ta cần return api.get(...) để lấy full response rồi bóc tách sau
    return api.get(`/vehicles?${params.toString()}`);
};