package com.oem.evpart.mappers;

import com.oem.evpart.dto.request.PartAllocationRequest;
import com.oem.evpart.dto.response.PartAllocationResponse;
import com.oem.evpart.models.PartAllocation;
import org.springframework.stereotype.Component;

@Component
public class PartAllocationMapper {

    /**
     * Chuyển từ Request DTO sang Entity.
     * Việc set đối tượng PartInventory sẽ được thực hiện ở tầng Service.
     */
    public PartAllocation toPartAllocation(PartAllocationRequest request) {
        return PartAllocation.builder()
                .serviceCenterId(request.getServiceCenterId())
                .allocatedQty(request.getAllocatedQty())
                .build();
    }

    /**
     * Chuyển từ Entity sang Response DTO.
     */
    public PartAllocationResponse toPartAllocationResponse(PartAllocation allocation) {
        return PartAllocationResponse.builder()
                .allocationId(allocation.getAllocationId())
                .inventoryId(allocation.getInventory().getInventoryId())
                .serviceCenterId(allocation.getServiceCenterId())
                .allocatedQty(allocation.getAllocatedQty())
                .allocatedDate(allocation.getAllocatedDate())
                .build();
    }
}