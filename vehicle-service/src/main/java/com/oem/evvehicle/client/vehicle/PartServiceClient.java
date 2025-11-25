package com.oem.evvehicle.client.vehicle;

import com.oem.evvehicle.client.vehicle.fallback.PartServiceClientFallback;
import com.oem.evvehicle.dto.external.PartInventoryDetailsDTO;
import com.oem.evvehicle.dto.request.DecrementStockRequest; // Import mới
import com.oem.evvehicle.dto.response.ApiResponse; // Import ApiResponse nếu bên Part trả về dạng này
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "part-service", fallback = PartServiceClientFallback.class)
public interface PartServiceClient {

    @GetMapping("/api/v1/inventory/part/{partId}")
    List<PartInventoryDetailsDTO> getInventoryByPartId(@PathVariable("partId") Long partId);

    // 🔥 THÊM MỚI: Gọi API trừ kho bên Part-Allocation-Controller
    @PostMapping("/api/v1/allocations/decrement")
    Object decrementStock(@RequestBody DecrementStockRequest request);
}