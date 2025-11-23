package com.oem.evwarranty.rabbitmq;

import com.oem.evwarranty.dto.event.VehicleCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class VehicleEventListener {

    // private final WarrantyRepository warrantyRepository; // Inject Repository

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    @Transactional
    public void handleVehicleCreated(VehicleCreatedEvent event) {
        log.info("📨 [Warranty Service] Received New Vehicle Event: VIN = {}", event.getVin());

        try {
            // LOGIC KÍCH HOẠT BẢO HÀNH (TODO: Bạn cần implement phần này)
            // 1. Parse ngày mua: LocalDateTime activationDate = LocalDateTime.parse(event.getPurchaseDate());
            // 2. Tính ngày hết hạn: LocalDateTime expirationDate = activationDate.plusYears(3);
            // 3. Tạo đối tượng Warranty:
            //    Warranty warranty = new Warranty();
            //    warranty.setVin(event.getVin());
            //    warranty.setStartDate(activationDate);
            //    warranty.setEndDate(expirationDate);
            //    warranty.setStatus("ACTIVE");

            // 4. Lưu vào DB: warrantyRepository.save(warranty);

            log.info("✅ [MOCK] Đã kích hoạt bảo hành 3 năm cho xe VIN: {}", event.getVin());

        } catch (Exception e) {
            log.error("❌ Lỗi khi xử lý sự kiện vehicle.created: {}", e.getMessage());
        }
    }
}
