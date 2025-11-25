package com.oem.evvehicle.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecrementStockRequest {
    private Long partId;
    private Integer quantity;
    private String location;
}