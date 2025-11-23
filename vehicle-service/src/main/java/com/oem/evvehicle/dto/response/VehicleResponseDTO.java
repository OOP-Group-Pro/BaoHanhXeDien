package com.oem.evvehicle.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class VehicleResponseDTO {
    private Long vehicleId;
    private String vehicleVin;
    private String model;
    private LocalDateTime manufacturedDate;
    private String status;
    private CustomerResponseDTO customer;// Lồng DTO của Customer vào đây
    private String licensePlate;
    private LocalDate warrantyStartDate; // Ngày kích hoạt bảo hành
    private Long currentOdometer;
}