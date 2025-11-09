package com.oem.evwarranty.client.vehicle.fallback;

import com.oem.evwarranty.client.vehicle.PartServiceClient;
import com.oem.evwarranty.dto.external.PartInventoryDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
@Component
@Slf4j
public class PartServiceClientFallback implements PartServiceClient {

    @Override
    public List<PartInventoryDetailsDTO> getInventoryByPartId(Long partId) {
        log.error("Lỗi Feign Client: Không thể gọi getInventoryByPartId cho partId: {}", partId);

        // Chiến lược an toàn nhất là trả về một danh sách rỗng.
        // Điều này giúp code của bạn không bị NullPointerException
        // và logic nghiệp vụ sẽ hiểu là "không tìm thấy kho".
        return Collections.emptyList();
    }
}