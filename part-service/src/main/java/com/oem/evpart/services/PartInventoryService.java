package com.oem.evpart.services;

import com.oem.evpart.dto.request.PartInventoryRequest;
import com.oem.evpart.dto.response.PartInventoryResponse;
import java.util.List;

public interface PartInventoryService {
    PartInventoryResponse addOrUpdateStock(PartInventoryRequest request);
    List<PartInventoryResponse> getInventoryByPartId(Long partId);
    List<PartInventoryResponse> getInventoryByLocation(String location);
    PartInventoryResponse updateInventoryStatus(Long inventoryId, String status);
    boolean isStockAvailable(Long partId, String location, int requiredQuantity);
}