package com.oem.evcampaign.rabbitmq;

import com.oem.evcampaign.dto.event.InventoryLowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InventoryEventListener {

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void handleInventoryLowEvent(InventoryLowEvent event) {
        log.info("📨 [Campaign Service] NHẬN CẢNH BÁO TỒN KHO TỪ RABBITMQ!");

        // In chi tiết ra để kiểm tra
        System.out.println("==================================================");
        System.out.println("⚠️ CẢNH BÁO NHẬP HÀNG GẤP");
        System.out.println("📦 Phụ tùng: " + event.getPartName());
        System.out.println("🔢 Mã Serial: " + event.getPartSerialNumber());
        System.out.println("🔻 Số lượng hiện tại: " + event.getCurrentTotalQuantity());
        System.out.println("💬 Ghi chú: " + event.getMessage());
        System.out.println("⏰ Thời gian: " + event.getTimestamp());
        System.out.println("==================================================");

        // Giả lập gửi email
        sendEmailToManager(event);
    }

    private void sendEmailToManager(InventoryLowEvent event) {
        // Logic gửi mail thật sẽ nằm ở đây (JavaMailSender)
        // Ở đây ta log ra console để demo
        log.info("📧 [MOCK EMAIL] Đang gửi email đến 'manager@oem.com'...");
        log.info("✅ Email đã được gửi thành công!");
    }
}