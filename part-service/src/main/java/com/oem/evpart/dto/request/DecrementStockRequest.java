package com.oem.evpart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO dùng để nhận yêu cầu trừ tồn kho sau khi một phụ tùng đã được sử dụng
 * (ví dụ: lắp đặt thành công).
 */
@Data
public class DecrementStockRequest {

    /**
     * ID của loại phụ tùng đã được sử dụng.
     */
    @NotNull(message = "Part ID không được để trống")
    private Long partId;

    /**
     * Tên địa điểm (Service Center) nơi phụ tùng được lấy ra sử dụng.
     * Phải khớp với trường 'location' trong bảng PartInventory.
     */
    @NotBlank(message = "Địa điểm (Location) không được để trống")
    private String location;

    /**
     * Số lượng đã sử dụng (thường là 1).
     */
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity; // Thường sẽ là 1
}
