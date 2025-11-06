package com.oem.evwarranty.dto;

import com.oem.evwarranty.model.WarrantyClaim;

public class ClaimPartDetailDto {

    private WarrantyClaim claim;

    private String partNumber;      // Mã số cho loại phụ tung -> dùng cái này để kiểm tra trong chính sách bảo hành và tồn kho
    private String partName;
    private Integer quantityRequired;       // Số lượng yêu cầu
}

