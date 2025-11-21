package com.oem.evpart.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allocation_id")
    private Long allocationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private PartInventory inventory;

    @Column(name = "service_center_id", nullable = false)
    private Long serviceCenterId; // FK từ service khác (User-Service)

    // 🔹 Thêm trường liên kết với Claim
    @Column(name = "claim_code", nullable = false)
    private String claimCode;

    @Column(name = "allocated_qty", nullable = false)
    private Long allocatedQty;

    // 🔹 Thêm trường số lượng đã giao (kho giao phụ tùng thực tế)
    @Column(name = "delivered_qty")
    private Long deliveredQty;

    // 🔹 Enum trạng thái cấp phát (đồng bộ với DTO AllocationStatus)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private AllocationStatus status;

    @Column(name = "allocated_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime allocatedDate;

    public enum AllocationStatus {
        PENDING,           // Đang chờ xử lý
        WAITING_FOR_PART,  // Đang chờ phụ tùng về
        READY_TO_INSTALL,  // Sẵn sàng lắp đặt
        NOT_REQUIRED       // Không yêu cầu phụ tùng
    }
}
