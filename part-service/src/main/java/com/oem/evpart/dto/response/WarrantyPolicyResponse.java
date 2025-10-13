package com.oem.evpart.dto.response;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Builder
@Data
public class WarrantyPolicyResponse {
    private Long policyId;
    private Long partId;
    private String partName;
    private Integer durationMonths;
    private Integer mileageLimit;
    private String conditions;
    private LocalDateTime createdAt;
}
