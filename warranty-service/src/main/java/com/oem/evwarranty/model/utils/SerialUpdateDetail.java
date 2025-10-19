package com.oem.evwarranty.model.utils;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SerialUpdateDetail {
    private String newSerialNumber;      // Số Seri phụ tùng mới lắp
    private String defectiveSerialNumber; // Số Seri phụ tùng hỏng tháo ra
}
