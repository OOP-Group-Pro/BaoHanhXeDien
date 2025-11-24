package com.oem.evwarranty.dto.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimApprovedEvent implements Serializable {
    private Long claimId;
    private String claimCode;
    private Long serviceCenterId;
    private List<PartItem> partsRequired;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PartItem implements Serializable {
        private String partNumber; // Mã phụ tùng (Serial hoặc Type tùy logic bạn chọn)
        private int quantity;
    }
}