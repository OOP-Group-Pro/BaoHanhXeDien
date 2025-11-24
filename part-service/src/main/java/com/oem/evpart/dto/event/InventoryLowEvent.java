package com.oem.evpart.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryLowEvent implements Serializable {
    private String partSerialNumber; // Mapping với Part.serialNumber
    private String partName;
    private Integer currentTotalQuantity;
    private String message;
    private String timestamp;
}