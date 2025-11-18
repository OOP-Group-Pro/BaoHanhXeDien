package com.oem.evvehicle.dto.external; // Package của vehicle-service

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO này dùng để "hứng" dữ liệu JSON trả về từ part-service.
 * Tên các trường PHẢI KHỚP TUYỆT ĐỐI với các trường JSON
 * trong PartInventoryResponse của part-service.
 */
@Data
public class PartInventoryDetailsDTO {

    // Khớp với "inventoryId"
    private Long inventoryId;

    // Khớp với "partId"
    private Long partId;

    // Khớp với "partName"
    private String partName;

    // Khớp với "quantity"
    private Integer quantity;

    // Khớp với "location"
    private String location;

    // Khớp với "status"
    private String status;

    // Khớp với "updatedAt"
    private LocalDateTime updatedAt;
}