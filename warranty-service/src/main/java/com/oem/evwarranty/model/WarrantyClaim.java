package com.oem.evwarranty.model;

import com.oem.evwarranty.enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarrantyClaim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 50)
    private String claimCode;
    @Column(nullable = false, length = 17)
    private String vin;         //Ma xe
    @Column(nullable = false)
    private Long scStaffId;     // Id Nhan vien tao don
    @Column(nullable = true)
    private Long technicalStaffId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClaimStatus currentStatus;
    @Column(nullable = false)
    private LocalDateTime dateCreated;
    @Column(nullable = true)
    private String description;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClaimStatusLog> statusLogs;
    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClaimCost> costs;
    @OneToMany(mappedBy = "claim",  cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttachedDocument> documents;
    @OneToMany(mappedBy = "claim",  cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClaimPartDetail> partDetails;

}
