package com.oem.evwarranty.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportDataDto {
    private String label; // Tên (Ví dụ: "Tháng 1", "Pin 50kWh", "Approved")
    private Long value;   // Giá trị (Số lượng)
}
