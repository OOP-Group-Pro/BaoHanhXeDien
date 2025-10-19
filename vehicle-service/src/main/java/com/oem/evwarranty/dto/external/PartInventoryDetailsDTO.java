package com.oem.evwarranty.dto.external;

import lombok.Data;

@Data
public class PartInventoryDetailsDTO {
    // Thông tin từ Part
    private Long partId;
    private String partName;
    private String manufacturer;
    private String partType;

    // Thông tin từ PartInventory
    private Integer quantityAvailable;
    private String location;
    private String status; // "Available", "Reserved", ...
}