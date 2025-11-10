package com.oem.evwarranty.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.oem.evwarranty.enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;
import com.oem.evwarranty.model.WarrantyClaim;
import org.w3c.dom.Text;

import java.time.LocalDateTime;

@Entity
@Table(name="claim_status_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimStatusLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="claim_id", nullable=false)
    private WarrantyClaim claim;

    @Column(nullable=false)
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private ClaimStatus status;
    @Column(nullable=false)
    private Long processorId;
    @Column(nullable = true, columnDefinition = "TEXT")
    private String notes;
}
