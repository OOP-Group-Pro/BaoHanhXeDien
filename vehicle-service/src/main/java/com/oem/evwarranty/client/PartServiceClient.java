package com.oem.evwarranty.client;

// Đảm bảo import DTO mới
import com.oem.evwarranty.dto.external.PartInventoryDetailsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "part-service")
public interface PartServiceClient {

    @GetMapping("/api/v1/inventory/part/{partId}")
    List<PartInventoryDetailsDTO> getInventoryByPartId(
            @PathVariable("partId") Long partId
    );
}