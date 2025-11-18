package com.oem.evvehicle.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VehicleResponseDTO {
    private Long vehicleId;
    private String vehicleVin;
    private String model;
    private LocalDateTime manufacturedDate;
    private String status;
    private CustomerResponseDTO customer;// Lồng DTO của Customer vào đây
    private String licensePlate;
}