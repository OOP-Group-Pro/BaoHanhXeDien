package com.oem.evwarranty.model.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteAllocationRequest {
    private String claimCode;

    // Danh sách các món hàng hỏng cần trả về kho
    private List<ReturnedPart> returnedParts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnedPart {
        private String partType; // Mã loại (VD: PIN) - Để tìm đúng dòng trong kho
        private int quantity;    // Thường là 1
    }
}