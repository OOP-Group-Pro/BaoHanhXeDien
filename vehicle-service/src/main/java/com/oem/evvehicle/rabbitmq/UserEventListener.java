package com.oem.evvehicle.rabbitmq;

import com.oem.evvehicle.dto.event.UserCreatedEvent;
import com.oem.evvehicle.entity.Customer;
import com.oem.evvehicle.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventListener {

    private final CustomerRepository customerRepository;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    @Transactional
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("📨[RabbitMQ] Nhận sự kiện User Created: {}", event);

        // 1. Chỉ xử lý nếu là Khách hàng (ROLE_USER)
        if ("ROLE_USER".equals(event.getRole())) {

            // 2. Check trùng lặp (Idempotency)
            if (customerRepository.existsByUserId(event.getUserId())) {
                log.warn("⚠ Customer đã tồn tại với UserID: {}", event.getUserId());
                return;
            }

            log.info("✅Phát hiện khách hàng mới (ID: {}). Đang lưu vào cơ sở dữ liệu...", event.getUserId());

            // 3. Map dữ liệu từ Event sang Entity Customer
            Customer newCustomer = new Customer();
            newCustomer.setUserId(event.getUserId()); // Quan trọng
            newCustomer.setCustomerName(event.getFullName());
            newCustomer.setEmail(event.getEmail());
            // newCustomer.setPhoneNum(event.getPhone()); // Nếu event có gửi kèm phone

            // 4. Lưu vào Database
            customerRepository.save(newCustomer);
            log.info("💾✅ Đã tạo hồ sơ Customer mới. ID: {}", newCustomer.getCustomerId());
        }
    }
}