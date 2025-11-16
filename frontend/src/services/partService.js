// Import "người gọi API" lõi (đã bao gồm Token)
import { api } from './apiClient.js';

// === API LẤY DỮ LIỆU (GET) ===

/**
 * Lấy danh sách tất cả Phụ tùng (cho Admin)
 * Tương ứng: PartController @GetMapping
 */
export const getParts = (page = 0, size = 10) =>
    api.get(`/parts?page=${page}&size=${size}`);

/**
 * Lấy TOÀN BỘ danh sách tồn kho (cho trang Quản lý Tồn kho)
 * Tương ứng: PartInventoryController @GetMapping("/all-stock")
 */
export const getAllInventory = () =>
    api.get('/inventory/all-stock');

/**
 * Lấy trạng thái cấp phát phụ tùng cho 1 Claim (cho Kỹ thuật viên)
 * Tương ứng: PartAllocationController @GetMapping("/status-by-claim/{claimId}")
 */
export const getAllocationStatusForClaim = (claimId) =>
    api.get(`/allocations/status-by-claim/${claimId}`);

// === API THAY ĐỔI DỮ LIỆU (POST, PATCH) ===

/**
 * Tạo một loại phụ tùng mới (cho Admin)
 * Tương ứng: PartController @PostMapping
 */
export const createPart = (partData) =>
    api.post('/parts', partData);

/**
 * Cập nhật số lượng tồn kho (cho Admin)
 * Tương ứng: PartInventoryController @PatchMapping("/{id}/quantity")
 */
export const updateInventoryQuantity = (inventoryId, newQuantity) =>
    api.patch(`/inventory/${inventoryId}/quantity`, { quantity: newQuantity });

/**
 * Yêu cầu cấp phát phụ tùng (do Warranty-Service gọi, nhưng Frontend cũng có thể gọi)
 * Tương ứng: PartController @PostMapping("/allocate-claim")
 */
export const requestAllocationForClaim = (allocationData) =>
    api.post('/parts/allocate-claim', allocationData);

/**
 * Trừ kho sau khi lắp đặt (do Vehicle-Service gọi)
 * Tương ứng: PartInventoryController @PostMapping("/decrement")
 */
export const decrementStock = (decrementData) =>
    api.post('/inventory/decrement', decrementData);