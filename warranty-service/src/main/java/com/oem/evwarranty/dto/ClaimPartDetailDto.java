package com.oem.evwarranty.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ClaimPartDetailDto {
    private String partNumber;      // Mã số cho loại phụ tung -> dùng cái này để kiểm tra trong chính sách bảo hành và tồn kho
    private String partName;
    private Integer quantityRequired;       // Số lượng yêu cầu
    private boolean isApproved;
    private String serialNumberDefective;
    private String serialNumberReplace;
}

