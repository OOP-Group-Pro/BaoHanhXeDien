package com.oem.evpart.dto.request;

import com.oem.evpart.models.Part;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyPolicyRequest {

    private List<Part> parts;

    private Integer durationMonths;

    private Integer mileageLimit;

    private String conditions;
}

