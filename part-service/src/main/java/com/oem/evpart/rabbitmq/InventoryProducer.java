package com.oem.evpart.rabbitmq;

import com.oem.evpart.dto.event.InventoryLowEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryProducer {

    private final RabbitTemplate rabbitTemplate;

    // Thêm mặc định cho exchange
    @Value("${app.rabbitmq.exchange:oem.internal.exchange}")
    private String exchange;

    // Giữ nguyên hoặc đảm bảo có mặc định cho routingKey
    @Value("${app.rabbitmq.routing-keys.inventory-low:inventory.low}")
    private String routingKey;

    public void sendInventoryLowEvent(InventoryLowEvent event) {
        log.warn("⚠️ [RabbitMQ] CẢNH BÁO TỒN KHO THẤP: {} (SL: {})",
                event.getPartName(), event.getCurrentTotalQuantity());
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
        } catch (Exception e) {
            log.error("❌ Lỗi gửi event tồn kho: {}", e.getMessage());
        }
    }
}