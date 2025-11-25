package com.oem.evpart.mappers;

import com.oem.evpart.dto.request.PartAllocationRequest;
import com.oem.evpart.dto.response.PartAllocationResponse;
import com.oem.evpart.models.Part;
import com.oem.evpart.models.PartAllocation;
import com.oem.evpart.models.PartInventory;
import org.springframework.stereotype.Component;

@Component
public class PartAllocationMapper {

    public PartAllocation toPartAllocation(PartAllocationRequest request) {
        // Hàm này chỉ map cơ bản, các trường phức tạp (Inventory, Status)
        // sẽ được set trong Service để đảm bảo logic.
        return PartAllocation.builder()
                .serviceCenterId(request.getServiceCenterId())
                .allocatedQty(request.getAllocatedQty())
                // Mặc định delivered là 0 khi mới tạo
                .deliveredQty(0L)
                .build();
    }

    public PartAllocationResponse toPartAllocationResponse(PartAllocation entity) {
        if (entity == null) return null;

        // Lấy thông tin liên kết (Tránh NullPointerException)
        PartInventory inventory = entity.getInventory();
        Part part = (inventory != null) ? inventory.getPart() : null;

        return PartAllocationResponse.builder()
                .allocationId(entity.getAllocationId())
                .claimCode(entity.getClaimCode())
                .serviceCenterId(entity.getServiceCenterId())

                // Map thông tin Part
                .partId(part != null ? part.getPartId() : null)
                .partName(part != null ? part.getName() : "Unknown Part")
                .serialNumber(part != null ? part.getSerialNumber() : "N/A")
                .partType(part != null ? part.getPartType() : "N/A")

                // Map thông tin Kho
                .inventoryId(inventory != null ? inventory.getInventoryId() : null)
                .sourceLocation(inventory != null ? inventory.getLocation() : "Unknown Location")

                // Map Số lượng & Trạng thái
                .allocatedQty(entity.getAllocatedQty())
                .deliveredQty(entity.getDeliveredQty())
                .status(entity.getStatus() != null ? entity.getStatus().name() : "UNKNOWN")
                .allocatedDate(entity.getAllocatedDate())
                .build();
    }
}