package com.oem.evwarranty.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="claim_costs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimCost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="claim_id")
    private WarrantyClaim claim;
    @Column(nullable=false)
    private String partNumber;
    @Column(nullable=true)
    private BigDecimal laborHours;
    @Column(nullable=true)
    private BigDecimal partCost;
    @Column(nullable=false)
    private BigDecimal totalCost;
    @Column(nullable=true)
    private LocalDate datePaid;
}
