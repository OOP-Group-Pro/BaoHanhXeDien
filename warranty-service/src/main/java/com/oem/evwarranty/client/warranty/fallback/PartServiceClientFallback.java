package com.oem.evwarranty.client.warranty.fallback;

import com.oem.evwarranty.client.warranty.PartServiceClient;
import com.oem.evwarranty.model.utils.PartAllocationRequest;
import com.oem.evwarranty.model.utils.PartResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PartServiceClientFallback implements PartServiceClient {
    @Override
    public void requestPartAllocation (PartAllocationRequest request) {
        log.error("Feign client ERROR: Cannot request part allocation");
    }

    @Override
    public String getPartType(@PathVariable("partId") Long partId) {
        log.error("Feign client ERROR: Cannot get part type");
        return null;
    }

    @Override
    public Map<String, PartResponseDto> getPartsByNumbers (List<String> partNumbers) {
        log.error("Feign client ERROR: Cannot get part by numbers");
        return null;
    }
}
