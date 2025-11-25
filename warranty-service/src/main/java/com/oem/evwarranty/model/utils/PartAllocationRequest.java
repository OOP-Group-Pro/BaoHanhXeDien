package com.oem.evwarranty.model.utils;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
// DTO phụ dùng để gửi yêu cầu cấp phát
public class PartAllocationRequest {
    private String claimCode;
    private Long serviceCenterId;
    private List<PartRequestItem> items;

    public PartAllocationRequest(Long claimId, List<String> partNumbers, Long centerId, Long scStaffId) {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartRequestItem {
        private String partNumber; // Mã SKU (serialNumber bên Part)
        private Integer quantity;
    }
}
