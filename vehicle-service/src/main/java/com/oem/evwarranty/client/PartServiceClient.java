package com.oem.evwarranty.client;

import com.oem.evwarranty.dto.external.PartInventoryDetailsDTO;
import com.oem.evwarranty.dto.request.AllocationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody; // Thêm import

@FeignClient(name = "part-service")
public interface PartServiceClient {

    /**
     * Kiểm tra tình trạng tồn kho của một loại linh kiện tại một địa điểm.
     */
    @GetMapping("/api/inventory/check")
    PartInventoryDetailsDTO checkAvailability(
            @RequestParam("partId") Long partId,
            @RequestParam("location") String location
    );

    /**
     * Yêu cầu phân bổ hoặc giảm trừ một linh kiện khỏi kho.
     * (Body có thể chứa partId, quantity, location...)
     */
    @PostMapping("/api/v1/inventory/allocate")
    ResponseEntity<Void> allocatePart(@RequestBody AllocationRequestDTO allocationRequest);
}