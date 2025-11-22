package com.oem.evpart.dto.request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO này dùng để nhận yêu cầu cấp phát từ Warranty-Service (của Đạt).
 * Nó khớp với DTO mà Đạt muốn gửi.
 */
@Data
public class ClaimAllocationRequest {

    @NotNull(message = "Claim ID không được để trống")
    private String claimCode;

    @NotNull(message = "Service Center ID không được để trống")
    private Long serviceCenterId; // Thống nhất là Integer

    private List<AllocationItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationItem {
        private String partNumber; // Mã SKU (serialNumber bên Part)
        private Integer quantity;
    }
}

