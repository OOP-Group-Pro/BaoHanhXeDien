package com.oem.evpart.config;

import com.oem.evpart.models.Part;
import com.oem.evpart.models.PartInventory;
import com.oem.evpart.models.WarrantyPolicy;
import com.oem.evpart.repositories.PartInventoryRepository;
import com.oem.evpart.repositories.PartRepository;
import com.oem.evpart.repositories.WarrantyPolicyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j // Thư viện để ghi log, giúp bạn thấy output trên console
public class PartDataLoader {

    @Bean
    CommandLineRunner loadData(PartRepository partRepository,
                               PartInventoryRepository inventoryRepository,
                               WarrantyPolicyRepository policyRepository) {
        return args -> {
            // Chỉ chạy khi database hoàn toàn trống (không có phụ tùng nào)
            if (partRepository.count() == 0) {
                log.info("Phát hiện database trống. Bắt đầu seeding dữ liệu mồi cho Part-Service...");

                // --- 1. Tạo Phụ tùng (Part) ---
                // (Tạo các loại phụ tùng "gốc")
                Part pin = Part.builder()
                        .name("Pin Lithium-Ion 50kWh")
                        .serialNumber("SN-PIN-LFP-50") // Serial number của loại part, không phải cái cụ thể
                        .manufacturer("OEM EV Solutions")
                        .partType("PIN")
                        .build();

                Part motor = Part.builder()
                        .name("Motor Điện 150kW")
                        .serialNumber("SN-MOTOR-R150")
                        .manufacturer("OEM EV Solutions")
                        .partType("MOTOR")
                        .build();

                Part bms = Part.builder()
                        .name("Hệ thống quản lý Pin (BMS) Gen 2")
                        .serialNumber("SN-BMS-GEN2")
                        .manufacturer("Advanced EV Tech")
                        .partType("BMS")
                        .build();

                // Lưu vào database và lấy lại đối tượng đã có partId
                Part savedPin = partRepository.save(pin);
                Part savedMotor = partRepository.save(motor);
                Part savedBms = partRepository.save(bms);
                log.info("Đã tạo 3 Part (Phụ tùng gốc).");

                // --- 2. Tạo Chính sách Bảo hành (WarrantyPolicy) ---
                // (Gán chính sách cho các phụ tùng vừa tạo)
                WarrantyPolicy pinPolicy = WarrantyPolicy.builder()
                        .part(savedPin) // Gán cho Pin
                        .durationMonths(24)
                        .mileageLimit(100000)
                        .conditions("Bảo hành 24 tháng hoặc 100.000km, tùy điều kiện nào đến trước.")
                        .build();

                WarrantyPolicy motorPolicy = WarrantyPolicy.builder()
                        .part(savedMotor) // Gán cho Motor
                        .durationMonths(36)
                        .mileageLimit(150000)
                        .conditions("Bảo hành 36 tháng hoặc 150.000km cho motor.")
                        .build();

                policyRepository.save(pinPolicy);
                policyRepository.save(motorPolicy);
                log.info("Đã tạo 2 WarrantyPolicy (Chính sách bảo hành).");

                // --- 3. Tạo Tồn kho (PartInventory) ---
                // (Nhập kho cho các phụ tùng vừa tạo)
                PartInventory pinInventory = PartInventory.builder()
                        .part(savedPin)
                        .quantity(100L)
                        .location("Kho Hà Nội")
                        .status(PartInventory.Status.Available) // Giả định bạn có Enum 'Status'
                        .build();

                PartInventory motorInventory = PartInventory.builder()
                        .part(savedMotor)
                        .quantity(50L)
                        .location("Kho Hà Nội")
                        .status(PartInventory.Status.Available)
                        .build();

                PartInventory bmsInventory = PartInventory.builder()
                        .part(savedBms)
                        .quantity(75L)
                        .location("Kho HCM")
                        .status(PartInventory.Status.Available)
                        .build();

                inventoryRepository.save(pinInventory);
                inventoryRepository.save(motorInventory);
                inventoryRepository.save(bmsInventory);
                log.info("Đã tạo 3 PartInventory (Bản ghi tồn kho).");

                log.info(">>> Hoàn tất seeding dữ liệu mồi. <<<");

            } else {
                log.info("Đã có dữ liệu. Bỏ qua seeding.");
            }
        };
    }
}
