package com.oem.evpart.dto.response;


import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartAllocationResponse {
    private Long allocationId;
    private Long inventoryId;
    private String partName;
    private Long serviceCenterId;
    private Integer allocatedQty;
    private LocalDateTime allocatedDate;
}

