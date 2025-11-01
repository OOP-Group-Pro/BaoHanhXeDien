package com.oem.evwarranty.client.fallback;

import com.oem.evwarranty.client.PartServiceClient;
import com.oem.evwarranty.model.utils.PartAllocationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PartServiceClientFallback implements PartServiceClient {
    @Override
    public void requestPartAllocation (PartAllocationRequest request) {
        log.error("Feign client ERROR: Cannot request part allocation");
    }
}
