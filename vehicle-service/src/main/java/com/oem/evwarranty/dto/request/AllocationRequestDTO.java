package com.oem.evwarranty.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO này được sử dụng bởi vehicle-service để gửi yêu cầu
 * phân bổ/giảm trừ một số lượng linh kiện từ kho của part-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllocationRequestDTO {

    /**
     * ID của loại linh kiện cần phân bổ (tương ứng với Part.partId).
     */
    private Long partId;

    /**
     * Số lượng linh kiện cần phân bổ/lấy ra.
     */
    private Integer quantity;

    /**
     * ID của Trung tâm Dịch vụ đang yêu cầu phân bổ linh kiện.
     * Dùng để truy vết và quản lý.
     */
    private Long serviceCenterId;

    /**
     * Vị trí kho mà linh kiện được lấy ra (ví dụ: "KHO_HCM", "KHO_HN").
     * Cần thiết để part-service tìm đúng record PartInventory.
     */
    private String location;
}