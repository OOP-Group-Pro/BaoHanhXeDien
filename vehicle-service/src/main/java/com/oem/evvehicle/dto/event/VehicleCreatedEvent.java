package com.oem.evvehicle.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleCreatedEvent implements Serializable {
    private Long vehicleId;
    private String vin;
    private String licensePlate;
    private Long ownerId; // ID của Customer
    private String purchaseDate; // Dùng String (ISO-8601) để tránh lỗi format ngày tháng qua JSON
}