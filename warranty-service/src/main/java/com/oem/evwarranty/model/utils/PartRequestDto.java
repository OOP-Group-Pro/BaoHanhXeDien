package com.oem.evwarranty.model.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// Class phụ để mô hình hóa yêu cầu phụ tùng
@Getter
@Setter
@AllArgsConstructor
public class PartRequestDto {
    private String partNumber;
    private String partName;
    private Integer quantity;
}
