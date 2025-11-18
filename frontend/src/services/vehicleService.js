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