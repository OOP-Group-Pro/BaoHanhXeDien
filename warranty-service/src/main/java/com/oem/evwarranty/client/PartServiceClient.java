package com.oem.evwarranty.client;


import com.oem.evwarranty.client.fallback.PartServiceClientFallback;
import com.oem.evwarranty.model.utils.PartAllocationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

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
}


