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

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Slf4j
public class PartDataLoader {

    @Bean
    CommandLineRunner loadData(PartRepository partRepository,
                               PartInventoryRepository inventoryRepository,
                               WarrantyPolicyRepository policyRepository) {
        return args -> {
            // Kiểm tra nếu chưa có dữ liệu thì mới chạy
            if (policyRepository.count() == 0 && partRepository.count() == 0) {
                log.info(">>> Bắt đầu seeding dữ liệu mồi cho Part-Service...");

                // ==========================================
                // 1. TẠO CHÍNH SÁCH BẢO HÀNH (Tạo trước)
                // ==========================================

                // Chính sách cho Pin (Lâu dài)
                WarrantyPolicy batteryPolicy = WarrantyPolicy.builder()
                        .durationMonths(24)      // 2 năm
                        .mileageLimit(100000)    // 100k km
                        .conditions("Bảo hành 24 tháng hoặc 100.000km, tùy điều kiện nào đến trước.")
                        .createdAt(LocalDateTime.now())
                        .build();

                // Chính sách Tiêu chuẩn (Động cơ, BMS...)
                WarrantyPolicy standardPolicy = WarrantyPolicy.builder()
                        .durationMonths(36)      // 3 năm
                        .mileageLimit(150000)    // 150k km
                        .conditions("Bảo hành 36 tháng hoặc 150.000km cho động cơ/hệ thống điện.")
                        .createdAt(LocalDateTime.now())
                        .build();

                // Lưu Policy trước để lấy ID
                WarrantyPolicy savedBatteryPolicy = policyRepository.save(batteryPolicy);
                WarrantyPolicy savedStandardPolicy = policyRepository.save(standardPolicy);
                log.info("✅ Đã tạo 2 WarrantyPolicy.");

                // ==========================================
                // 2. TẠO PHỤ TÙNG (PART) - GÁN POLICY VÀO PART
                // ==========================================

                // Part 1: Pin (Gán chính sách Pin)
                Part pin = Part.builder()
                        .name("Pin Lithium-Ion 50kWh")
                        .serialNumber("SN-PIN-LFP-50")
                        .manufacturer("OEM EV Solutions")
                        .partType("PIN")
                        .price(2600000d)
                        .warrantyPolicy(savedBatteryPolicy) // <--- Gán Policy vào đây
                        .createdAt(LocalDateTime.now())
                        .build();

                // Part 2: Motor (Gán chính sách Tiêu chuẩn)
                Part motor = Part.builder()
                        .name("Motor Điện 150kW")
                        .serialNumber("SN-MOTOR-R150")
                        .manufacturer("OEM EV Solutions")
                        .partType("MOTOR")
                        .price(3250000d)
                        .warrantyPolicy(savedStandardPolicy) // <--- Gán Policy vào đây
                        .createdAt(LocalDateTime.now())
                        .build();

                // Part 3: BMS (Gán chính sách Tiêu chuẩn)
                Part bms = Part.builder()
                        .name("Hệ thống quản lý Pin (BMS) Gen 2")
                        .serialNumber("SN-BMS-GEN2")
                        .manufacturer("Advanced EV Tech")
                        .partType("BMS")
                        .price(1500000d) // Thêm giá ví dụ
                        .warrantyPolicy(savedStandardPolicy) // <--- Gán Policy vào đây
                        .createdAt(LocalDateTime.now())
                        .build();

                // Lưu Part
                Part savedPin = partRepository.save(pin);
                Part savedMotor = partRepository.save(motor);
                Part savedBms = partRepository.save(bms);
                log.info("✅ Đã tạo 3 Part (đã liên kết Policy).");

                // ==========================================
                // 3. TẠO TỒN KHO (PART INVENTORY)
                // ==========================================
                PartInventory pinInventory = PartInventory.builder()
                        .part(savedPin)
                        .quantity(100L)
                        .location("Kho Hà Nội")
                        .status(PartInventory.Status.Available)
                        .updatedAt(LocalDateTime.now())
                        .build();

                PartInventory motorInventory = PartInventory.builder()
                        .part(savedMotor)
                        .quantity(50L)
                        .location("Kho Hà Nội")
                        .status(PartInventory.Status.Available)
                        .updatedAt(LocalDateTime.now())
                        .build();

                PartInventory bmsInventory = PartInventory.builder()
                        .part(savedBms)
                        .quantity(75L)
                        .location("Kho HCM")
                        .status(PartInventory.Status.Available)
                        .updatedAt(LocalDateTime.now())
                        .build();

                inventoryRepository.saveAll(List.of(pinInventory, motorInventory, bmsInventory));
                log.info("✅ Đã tạo 3 PartInventory.");

                log.info(">>> Hoàn tất seeding dữ liệu mồi. <<<");

            } else {
                log.info("Đã có dữ liệu. Bỏ qua seeding.");
            }
        };
    }
}