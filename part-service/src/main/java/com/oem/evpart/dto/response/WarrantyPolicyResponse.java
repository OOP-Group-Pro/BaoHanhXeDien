package com.oem.evpart.dto.response;


import com.oem.evpart.models.Part;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class WarrantyPolicyResponse {
    private Long policyId;
    private List<Part> parts;
    private String partName;
    private Integer durationMonths;
    private Integer mileageLimit;
    private String conditions;
    private LocalDateTime createdAt;
}
