package com.oem.evuser.rabbitmq;


import com.oem.evuser.dto.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    // Lấy routing key từ yml, nếu lỗi thì dùng mặc định "user.created"
    @Value("${app.rabbitmq.routing-keys.user-created:user.created}")
    private String routingKey;

    public void sendUserCreatedEvent(UserCreatedEvent event) {
        log.info("🚀 [RabbitMQ] Đang gửi sự kiện UserCreatedEvent: {}", event);
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("✅ [RabbitMQ] Gửi thành công tới Exchange: {}", exchange);
        } catch (Exception e) {
            log.error("❌ [RabbitMQ] Gửi thất bại: {}", e.getMessage());
        }
    }
}