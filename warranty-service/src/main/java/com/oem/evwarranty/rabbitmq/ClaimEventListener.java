package com.oem.evwarranty.rabbitmq;

import com.oem.evwarranty.dto.event.ClaimApprovedEvent;
import com.oem.evpart.services.PartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClaimEventListener {

    private final PartService partService;

    @RabbitListener(queues = "${app.rabbitmq.consumer.queue:part.claim.approved.queue}")
    @Transactional
    public void handleClaimApproved(ClaimApprovedEvent event) {
        log.info("📨 [Part Service] Nhận lệnh xuất kho cho Claim: {}", event.getClaimCode());

        if (event.getPartsRequired() == null || event.getPartsRequired().isEmpty()) {
            log.warn("⚠️ Claim {} không yêu cầu phụ tùng nào.", event.getClaimCode());
            return;
        }

        // Duyệt qua danh sách phụ tùng cần thay
        for (ClaimApprovedEvent.PartItem item : event.getPartsRequired()) {
            try {
                log.info("🛠️ Đang trừ kho: {} - SL: {}", item.getPartNumber(), item.getQuantity());

                // Gọi hàm trừ kho (Hàm này đã bao gồm logic check tồn kho thấp -> bắn campaign)
                // Lưu ý: Hàm deductStock của bạn hiện nhận 'serialNumber' làm partNumber
                partService.deductStock(item.getPartNumber(), item.getQuantity());

            } catch (Exception e) {
                log.error("❌ Lỗi xuất kho phụ tùng {} cho Claim {}: {}",
                        item.getPartNumber(), event.getClaimCode(), e.getMessage());
                // Tùy nghiệp vụ: Có thể throw exception để RabbitMQ retry, hoặc log lại để xử lý tay
            }
        }

        log.info("✅ Hoàn tất xuất kho cho Claim: {}", event.getClaimCode());
    }
}