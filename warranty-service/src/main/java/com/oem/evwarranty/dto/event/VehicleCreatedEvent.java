package com.oem.evwarranty.dto.event;

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
    private Long ownerId;
    private String purchaseDate; // Dạng String ISO-8601
}