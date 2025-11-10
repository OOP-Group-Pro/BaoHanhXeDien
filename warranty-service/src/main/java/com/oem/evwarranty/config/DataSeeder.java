package com.oem.evwarranty.config;

import com.oem.evwarranty.enums.ClaimStatus;
import com.oem.evwarranty.model.*;
import com.oem.evwarranty.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    // Tự động inject tất cả các repository bạn cần
    private final WarrantyClaimRepository warrantyClaimRepository;
    // Bạn không cần inject các repository con (như ClaimStatusLogRepository, ...)
    // vì chúng ta sẽ dùng CascadeType.ALL từ WarrantyClaim

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra xem đã có dữ liệu chưa, nếu có rồi thì không seed nữa
        if (warrantyClaimRepository.count() > 0) {
            log.info("Data already exists. Skipping seeding.");
            return;
        }

        log.info("Seeding sample data...");
        createSampleData();
        log.info("Data seeding finished.");
    }

    private void createSampleData() {
        // === TẠO CLAIM 1:.WAITING_APPROVAL (Đang chờ xử lý) ===

        // 1. Tạo đối tượng cha (WarrantyClaim)
        WarrantyClaim claim1 = WarrantyClaim.builder()
                .claimCode("WC-2025-0001")
                .vin("VIN123456789ABCDE")
                .scStaffId(1L) // ID của nhân viên SC (giả định)
                .centerId(101L)
                .technicalStaffId(10L) // ID của kỹ thuật viên (giả định)
                .currentStatus(ClaimStatus.WAITING_APPROVAL)
                .dateCreated(LocalDateTime.now().minusDays(1))
                .description("Xe phát ra tiếng kêu lạ ở trục trước khi tăng tốc.")
                .build();

        // 2. Tạo các đối tượng con

        // Log trạng thái
        ClaimStatusLog log1 = ClaimStatusLog.builder()
                .claim(claim1) // <== Gán cha cho con
                .timestamp(claim1.getDateCreated())
                .status(ClaimStatus.WAITING_APPROVAL)
                .processorId(1L) // Nhân viên SC đã tạo
                .notes("Tiếp nhận yêu cầu bảo hành từ khách hàng.")
                .build();

        // Chi tiết phụ tùng yêu cầu
        ClaimPartDetail part1 = ClaimPartDetail.builder()
                .claim(claim1) // <== Gán cha cho con
                .partNumber("PN-TRUC-TRUOC-A1")
                .quantityRequired(1)
                .serialNumberDefective("DEFECT-SERIAL-001")
                .isApproved(false) // Chưa được duyệt
                .build();

        // Tài liệu đính kèm
        AttachedDocument doc1 = new AttachedDocument();
        doc1.setClaim(claim1); // <== Gán cha cho con
        doc1.setFileName("video-tieng-keu.mp4");
        doc1.setFileType("video/mp4");
        doc1.setStoragePath("/uploads/claims/WC-2025-0001/video-tieng-keu.mp4");
        doc1.setUploadDate(LocalDateTime.now().minusDays(1));

        // 3. Gán danh sách con vào cho cha
        claim1.setStatusLogs(List.of(log1));
        claim1.setPartDetails(List.of(part1));
        claim1.setDocuments(List.of(doc1));

        // 4. Lưu cha (do có CascadeType.ALL, tất cả con sẽ được lưu theo)
        warrantyClaimRepository.save(claim1);


        // === TẠO CLAIM 2: COMPLETED (Đã hoàn thành) ===

        // 1. Tạo đối tượng cha
        WarrantyClaim claim2 = WarrantyClaim.builder()
                .claimCode("WC-2025-0002")
                .vin("VIN987654321ZYXWV")
                .scStaffId(2L)
                .centerId(102L)
                .technicalStaffId(11L)
                .currentStatus(ClaimStatus.COMPLETED)
                .dateCreated(LocalDateTime.now().minusWeeks(2))
                .description("Hệ thống pin báo lỗi sạc không vào.")
                .build();

        // 2. Tạo các đối tượng con

        // Lịch sử trạng thái (nhiều log)
        ClaimStatusLog log2_1_pending = ClaimStatusLog.builder()
                .claim(claim2)
                .timestamp(claim2.getDateCreated())
                .status(ClaimStatus.WAITING_APPROVAL)
                .processorId(2L)
                .notes("Tiếp nhận xe, chờ kiểm tra.")
                .build();
        ClaimStatusLog log2_2_approved = ClaimStatusLog.builder()
                .claim(claim2)
                .timestamp(claim2.getDateCreated().plusDays(1))
                .status(ClaimStatus.APPROVED)
                .processorId(5L) // Manager duyệt
                .notes("Đã duyệt thay thế cụm pin theo chính sách.")
                .build();
        ClaimStatusLog log2_3_completed = ClaimStatusLog.builder()
                .claim(claim2)
                .timestamp(claim2.getDateCreated().plusDays(3))
                .status(ClaimStatus.COMPLETED)
                .processorId(11L) // Kỹ thuật viên hoàn thành
                .notes("Đã thay pin mới, xe hoạt động bình thường.")
                .build();

        // Chi tiết phụ tùng
        ClaimPartDetail part2 = ClaimPartDetail.builder()
                .claim(claim2)
                .partNumber("PN-BATTERY-EV-XL")
                .quantityRequired(1)
                .serialNumberReplace("NEW-SERIAL-BATT-002") // Đã được thay
                .serialNumberDefective("DEFECT-SERIAL-BATT-005")
                .isApproved(true) // Đã được duyệt
                .build();

        // Chi phí
        ClaimCost cost2 = new ClaimCost();
        cost2.setClaim(claim2);
        cost2.setPartNumber("PN-BATTERY-EV-XL");
        cost2.setLaborHours(new BigDecimal("4.5"));
        cost2.setPartCost(new BigDecimal("8500.00"));
        cost2.setTotalCost(new BigDecimal("8950.00")); // Giả sử $450 tiền công
        cost2.setDatePaid(LocalDate.now().minusDays(10));

        // 3. Gán danh sách con vào cho cha
        claim2.setStatusLogs(List.of(log2_1_pending, log2_2_approved, log2_3_completed));
        claim2.setPartDetails(List.of(part2));
        claim2.setCosts(List.of(cost2));

        // 4. Lưu cha
        warrantyClaimRepository.save(claim2);
    }
}