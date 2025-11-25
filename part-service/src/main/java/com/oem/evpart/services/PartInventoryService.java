package com.oem.evpart.services;

import com.oem.evpart.dto.request.DecrementStockRequest;
import com.oem.evpart.dto.request.PartInventoryRequest;
import com.oem.evpart.dto.response.PageCacheDto;
import com.oem.evpart.dto.response.PartInventoryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PartInventoryService {
    /**
     * Thêm hoặc cập nhật số lượng tồn kho (nhập kho).
     */
    PartInventoryResponse addOrUpdateStock(PartInventoryRequest request);

    /**
     * Trừ tồn kho sau khi phụ tùng đã được sử dụng.
     */
    PartInventoryResponse decrementStock(DecrementStockRequest request); // Thêm phương thức này

    List<PartInventoryResponse> getInventoryByPartId(Long partId);
    List<PartInventoryResponse> getInventoryByLocation(String location);
    PartInventoryResponse updateInventoryStatus(Long inventoryId, String status);

    /**
     * Kiểm tra xem còn đủ hàng tại một địa điểm cụ thể không.
     * (Hàm này có thể không cần nữa nếu logic kiểm tra được chuyển sang hàm decrementStock)
     */
    boolean isStockAvailable(Long partId, String location, int requiredQuantity);

    // com.oem.evpart.services.PartInventoryService

    // API lấy toàn bộ tồn kho (có phân trang) cho Admin
    PageCacheDto<PartInventoryResponse> getAllInventories(Pageable pageable);
}