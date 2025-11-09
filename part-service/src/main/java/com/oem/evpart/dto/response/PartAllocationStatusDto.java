package com.oem.evpart.dto.response;

import com.oem.evpart.models.PartAllocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PartAllocationStatusDto {

    // ID của Claim để Giao diện xác nhận
    private Long claimId;

    // Trạng thái cấp phát
    private PartAllocation.AllocationStatus status;


}
