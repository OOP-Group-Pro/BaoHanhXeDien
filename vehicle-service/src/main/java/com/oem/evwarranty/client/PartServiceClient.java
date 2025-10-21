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

    @GetMapping("/api/inventory/check")
    PartInventoryDetailsDTO checkAvailability(
            @RequestParam("partId") Long partId,
            @RequestParam("location") String location
    );

    @PostMapping("/api/v1/inventory/allocate")
    ResponseEntity<Void> allocatePart(@RequestBody AllocationRequestDTO allocationRequest);
}