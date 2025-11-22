package com.oem.evvehicle.rabbitmq;

import com.oem.evvehicle.dto.event.VehicleCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    // Routing key cho sự kiện tạo xe
    // Dùng giá trị mặc định "vehicle.created" nếu chưa cấu hình trong yml
    @Value("${app.rabbitmq.routing-keys.vehicle-created:vehicle.created}")
    private String routingKey;

    public void sendVehicleCreatedEvent(VehicleCreatedEvent event) {
        log.info("🚀 [RabbitMQ] Sending VehicleCreatedEvent for VIN: {}", event.getVin());
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("✅ Event sent successfully to Exchange: {}", exchange);
        } catch (Exception e) {
            log.error("❌ Failed to send VehicleCreatedEvent: {}", e.getMessage());
        }
    }
}