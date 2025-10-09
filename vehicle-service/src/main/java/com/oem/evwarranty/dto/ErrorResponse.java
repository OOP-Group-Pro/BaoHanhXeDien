package com.oem.evwarranty.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private int status; // Ví dụ: 404, 500
    private String message; // Thông báo lỗi
    private long timestamp; // Thời gian xảy ra lỗi
}