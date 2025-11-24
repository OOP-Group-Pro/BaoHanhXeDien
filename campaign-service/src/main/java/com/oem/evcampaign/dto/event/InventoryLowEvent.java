package com.oem.evcampaign.dto.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryLowEvent implements Serializable {
    private String partSerialNumber; // Khớp với Part Service
    private String partName;
    private Integer currentTotalQuantity; // Khớp với Part Service
    private String message;
    private String timestamp;
}
