package com.oem.evwarranty.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarrantyRequest {
    private Long userId;
    private Long productId;
    private String serialNumber;
    private String description;
}
