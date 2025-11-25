package com.oem.evwarranty.client.warranty;


import com.oem.evwarranty.client.warranty.fallback.PartServiceClientFallback;
import com.oem.evwarranty.config.FeignConfig;
import com.oem.evwarranty.model.utils.PartAllocationRequest;
import com.oem.evwarranty.model.utils.PartResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * Feign Client cho Part-Service
 */
@FeignClient(
        name = "part-service",
        url = "${part-service.url}",
        configuration = FeignConfig.class,
        fallback = PartServiceClientFallback.class
)
public interface PartServiceClient {

    // Gửi yêu cầu Part Allocation (cấp phát phụ tùng) sau khi Claim được duyệt
    @PostMapping("/api/v1/parts/allocate-claim")
    public void requestPartAllocation(@RequestBody PartAllocationRequest request);

    @GetMapping("/api/v1/parts/{partId}/partType")
    public String getPartType(@PathVariable("partId") Long partId);

    @PostMapping("/api/v1/parts/by-numbers")
    Map<String, PartResponseDto> getPartsByNumbers(@RequestBody List<String> partNumbers);
}


