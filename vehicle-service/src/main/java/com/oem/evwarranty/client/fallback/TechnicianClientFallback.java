package com.oem.evwarranty.client.fallback;

import com.oem.evwarranty.client.TechnicianClient;
import com.oem.evwarranty.dto.external.TechnicianDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TechnicianClientFallback implements TechnicianClient {
    @Override
    public TechnicianDetailsDTO getTechnicianDetails(Long userId) {

        // Ghi log lỗi
        log.error("Lỗi Feign Client: Không thể gọi getTechnicianDetails cho userId: {}", userId);

        // --- Chiến lược Fallback ---
        // Trả về một đối tượng DTO "mặc định".
        // Điều này cực kỳ quan trọng để ServiceHistoryService
        // của bạn không bị NullPointerException.

        TechnicianDetailsDTO defaultDto = new TechnicianDetailsDTO();
        defaultDto.setFullName("Không rõ KTV"); // Tên mặc định
        defaultDto.setServiceCenterId(null);


        return defaultDto;
    }
}