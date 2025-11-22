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

    // Nếu không tìm thấy trong yml, nó sẽ dùng "user.created" làm mặc định
    @Value("${app.rabbitmq.routing-keys.user-created:user.created}")
    private String routingKey;

    public void sendUserCreatedEvent(UserCreatedEvent event) {
        log.info("🚀 [RabbitMQ] Sending UserCreatedEvent: {}", event);
        // Tự động convert sang JSON nhờ config ở Bước 3
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}