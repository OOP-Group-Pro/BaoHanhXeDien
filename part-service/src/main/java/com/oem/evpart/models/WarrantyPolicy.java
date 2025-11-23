package com.oem.evpart.models;

import com.oem.evpart.models.Part;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long policyId;

    @OneToMany(mappedBy = "warrantyPolicy")
    private List<Part> parts;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(name = "mileage_limit")
    private Integer mileageLimit;

    @Column(columnDefinition = "TEXT")
    private String conditions;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}

