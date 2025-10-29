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

    @Column(name = "allocated_qty", nullable = false)
    private Integer allocatedQty;

    @Column(name = "allocated_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime allocatedDate;
}

