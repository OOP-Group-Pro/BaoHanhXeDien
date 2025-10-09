package com.oem.evwarranty.dto;

import lombok.Data;

@Data
public class VehicleRequestDTO {
    private String vehicleVin;
    private String model;
    // ... các thuộc tính khác của Vehicle bạn muốn người dùng nhập vào

    private Long customerId; // Chỉ cần ID của customer
}