package com.oem.evpart.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "part_id")
    private Long partId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "serial_number", unique = true, length = 100)
    private String serialNumber;

    @Column(length = 100)
    private String manufacturer;

    @Column(name = "part_type", length = 50)
    private String partType;

    @Column(name = "price")
    private Double price;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // ===== Quan hệ =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warranty_policy_id")
    private WarrantyPolicy warrantyPolicy;


    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PartInventory> inventories;

    @PrePersist
    public void prePersist() { createdAt = LocalDateTime.now(); }
}
