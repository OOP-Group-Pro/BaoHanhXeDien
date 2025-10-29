package com.oem.evwarranty.client;

// Đảm bảo import DTO mới
import com.oem.evwarranty.dto.external.PartInventoryDetailsDTO;
import com.oem.evwarranty.dto.request.AllocationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "part-service")
public interface PartServiceClient {

    @GetMapping("/api/v1/inventory/part/{partId}")
    List<PartInventoryDetailsDTO> getInventoryByPartId(
            @PathVariable("partId") Long partId
    );
    /**
     * Gọi sang part-service để yêu cầu trừ tồn kho
     * sau khi một linh kiện đã được lắp đặt.
     **/
    @PostMapping("/api/v1/inventory/allocate") // (Đường dẫn này 2 bên phải thống nhất)
    void allocateStock(@RequestBody AllocationRequestDTO request);
}