package com.oem.evwarranty.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VehicleRequestDTO {
    @NotBlank(message = "VIN không được để trống")
    @Size(min = 17, max = 17, message = "VIN phải có đúng 17 ký tự")
    private String vehicleVin;

    @NotBlank
    private String model;
    private LocalDateTime manufacturedDate;

    private String status;
    private String licensePlate;
    private Long customerId; // Chỉ cần ID của customer
}