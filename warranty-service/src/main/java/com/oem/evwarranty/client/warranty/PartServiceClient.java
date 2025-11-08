package com.oem.evwarranty.client.warranty;


import com.oem.evwarranty.client.warranty.fallback.PartServiceClientFallback;
import com.oem.evwarranty.model.utils.PartAllocationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client cho Part-Service
 */
@FeignClient(
        name = "part-service",
        url = "${part-service.url}",
        fallback = PartServiceClientFallback.class
)
public interface PartServiceClient {

    // Gửi yêu cầu Part Allocation (cấp phát phụ tùng) sau khi Claim được duyệt
    @PostMapping("/api/v1/parts/allocate-claim")
    public void requestPartAllocation(@RequestBody PartAllocationRequest request);

    @GetMapping("/api/v1/parts/{partId}/partType")
    public String getPartType(@PathVariable("partId") Long partId);
}


