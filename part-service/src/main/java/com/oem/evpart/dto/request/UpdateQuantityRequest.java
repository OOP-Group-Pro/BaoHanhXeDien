package com.oem.evpart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// DTO này chỉ dùng để nhận 1 trường duy nhất khi cập nhật số lượng
@Data
public class UpdateQuantityRequest {

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng không được là số âm")
    private Long quantity;
}
