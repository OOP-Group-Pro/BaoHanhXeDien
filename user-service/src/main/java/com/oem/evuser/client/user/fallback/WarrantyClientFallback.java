package com.oem.evuser.client.user.fallback;

import com.oem.evuser.dto.request.WarrantyRequest;
import com.oem.evuser.dto.response.WarrantyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.oem.evuser.client.user.WarrantyClient;

@Component
@Slf4j
public class WarrantyClientFallback implements WarrantyClient {

    @Override
    public ResponseEntity<Void> createWarranty(WarrantyRequest request) {
        log.error("⚠️ [Fallback] Không thể gọi WarrantyService để tạo warranty cho: {}", request);
        // Không tạo được thì trả về 200 rỗng hoặc 503 tùy logic bạn muốn
        return ResponseEntity.status(503).build();
    }

    @Override
    public WarrantyResponse getWarrantyById(Long id) {
        log.error("⚠️ [Fallback] Không thể gọi WarrantyService để lấy warranty id: {}", id);

        // Trả về đối tượng mặc định để service không crash
        WarrantyResponse fallbackResponse = new WarrantyResponse();
        fallbackResponse.setId(id);
        fallbackResponse.setStatus("UNKNOWN");
        fallbackResponse.setDescription("Dịch vụ bảo hành không khả dụng");

        return fallbackResponse;
    }
}
