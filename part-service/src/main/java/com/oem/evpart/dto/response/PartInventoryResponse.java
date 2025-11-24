package com.oem.evpart.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartInventoryResponse {
    private Long inventoryId;
    private Long partId;
    private String partName;
    private String serialNumber;
    private Double price;
    private String partType;
    private Long quantity;
    private String location;
    private String status;
    private LocalDateTime updatedAt;
}

