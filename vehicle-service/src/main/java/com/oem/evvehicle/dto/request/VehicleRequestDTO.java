package com.oem.evvehicle.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class VehicleRequestDTO {
    @Size(min = 17, max = 17, message = "VIN phải có đúng 17 ký tự")
    private String vehicleVin;


    private String model;
    private LocalDateTime manufacturedDate;

    private String status;
    private String licensePlate;
    private Long customerId; // Chỉ cần ID của customer
    private LocalDate warrantyStartDate; // Ngày kích hoạt bảo hành
    private Long currentOdometer;
}