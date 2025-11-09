package com.oem.evwarranty.client.vehicle.fallback;

import com.oem.evwarranty.client.vehicle.CenterClient;
import com.oem.evwarranty.dto.external.CenterDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CenterClientFallback implements CenterClient {

    @Override
    public CenterDetailsDTO getCenterDetails(Long centerId) {

        // Log lại lỗi để đội vận hành (DevOps) biết
        log.error("Lỗi Feign Client: Không thể gọi getCenterDetails cho centerId: {}", centerId);

        // --- Chiến lược Fallback ---
        // Trả về một DTO "mặc định" (default)
        // để service của bạn không bị sập (crash).

        CenterDetailsDTO defaultDto = new CenterDetailsDTO();
        defaultDto.setCenterId(centerId);
        defaultDto.setCenterName("Không rõ trung tâm");
        defaultDto.setAddress("N/A");

        return defaultDto;
    }
}