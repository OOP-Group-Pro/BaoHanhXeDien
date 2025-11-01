package com.oem.evwarranty.model.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
// DTO phụ dùng để gửi yêu cầu cấp phát
public class PartAllocationRequest {
    private Long claimId;
    private List<String> partNumbers;
    private Long serviceCenterId;
    private Long requestingStaffId;
    // ... (Thêm serviceCenterId để PartService biết gửi về đâu)
}
