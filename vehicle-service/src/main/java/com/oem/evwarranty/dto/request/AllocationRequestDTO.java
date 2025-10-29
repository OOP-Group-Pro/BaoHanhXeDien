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

    private Long partId;

    private Long quantity;

    private String location;
}