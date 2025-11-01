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
    private Long quantity;
    private String location;
    private String status;
    private LocalDateTime updatedAt;
}

