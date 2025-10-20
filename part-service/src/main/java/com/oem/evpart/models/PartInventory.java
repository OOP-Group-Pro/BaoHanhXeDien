package com.oem.evpart.models;

import com.oem.evpart.models.Part;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "PartInventory",
        uniqueConstraints = @UniqueConstraint(columnNames = {"part_id", "location"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Available','Reserved','Defective') DEFAULT 'Available'")
    private Status status = Status.Available;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // ===== Quan hệ =====
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartAllocation> allocations;

    public enum Status {
        Available, Reserved, Defective
    }
}
