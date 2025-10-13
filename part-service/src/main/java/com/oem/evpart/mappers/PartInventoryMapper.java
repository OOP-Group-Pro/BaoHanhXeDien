package com.oem.evpart.mappers;

import com.oem.evpart.dto.request.PartInventoryRequest;
import com.oem.evpart.dto.response.PartInventoryResponse;
import com.oem.evpart.models.PartInventory;
import org.springframework.stereotype.Component;

@Component
public class PartInventoryMapper {

    /**
     * Chuyển từ Request DTO (dùng String) sang Entity (dùng Enum).
     * Việc set đối tượng Part sẽ được thực hiện ở tầng Service.
     */
    public PartInventory toPartInventory(PartInventoryRequest request) {
        return PartInventory.builder()
                .quantity(request.getQuantity())
                .location(request.getLocation())

                //  nếu status không được cung cấp, mặc định là 'Available'
                .status(request.getStatus() != null ?
                        PartInventory.Status.valueOf(request.getStatus()) :
                        PartInventory.Status.Available)
                .build();
    }


    public PartInventoryResponse toPartInventoryResponse(PartInventory inventory) {
        return PartInventoryResponse.builder()
                .inventoryId(inventory.getInventoryId())
                .partId(inventory.getPart().getPartId())
                .partName(inventory.getPart().getName())
                .quantity(inventory.getQuantity())
                .location(inventory.getLocation())
                // Chuyển Enum từ Entity thành String cho Response
                .status(inventory.getStatus().name())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}