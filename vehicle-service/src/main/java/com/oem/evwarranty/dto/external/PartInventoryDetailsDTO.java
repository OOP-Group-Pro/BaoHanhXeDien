package com.oem.evwarranty.dto.external;

import lombok.Data;

/**
 * DTO này dùng để "hứng" dữ liệu trả về từ part-service.
 * Các trường của nó PHẢI KHỚP với cấu trúc JSON
 * của class PartInventoryResponse bên part-service.
 */
@Data
public class PartInventoryDetailsDTO {
    private Long inventoryId;
    private Integer quantity;
    private String location;
    private String status; // "Available", "Reserved", "Defective"

    // Thông tin lồng nhau từ Part
    private PartInfo part;


    @Data
    public static class PartInfo {
        private Long partId;
        private String name;
        private String serialNumber;
        private String manufacturer;
        private String partType;
    }
}