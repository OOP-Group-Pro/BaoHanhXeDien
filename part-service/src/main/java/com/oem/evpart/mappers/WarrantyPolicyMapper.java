package com.oem.evpart.mappers;

import com.oem.evpart.dto.request.WarrantyPolicyRequest;
import com.oem.evpart.dto.response.WarrantyPolicyResponse;
import com.oem.evpart.models.WarrantyPolicy;
import org.springframework.stereotype.Component;

@Component
public class WarrantyPolicyMapper {

    /**
     * Chuyển từ Request DTO sang Entity.
     * Lưu ý: Việc set đối tượng Part sẽ được thực hiện ở tầng Service.
     */
    public WarrantyPolicy toWarrantyPolicy(WarrantyPolicyRequest request) {
        return WarrantyPolicy.builder()
                .durationMonths(request.getDurationMonths())
                .mileageLimit(request.getMileageLimit())
                .conditions(request.getConditions())
                .build();
    }

    /**
     * Chuyển từ Entity sang Response DTO.
     */
    public WarrantyPolicyResponse toWarrantyPolicyResponse(WarrantyPolicy policy) {
        return WarrantyPolicyResponse.builder()
                .policyId(policy.getPolicyId())
                .parts(policy.getParts()) // Lấy ID từ đối tượng Part liên kết
                .durationMonths(policy.getDurationMonths())
                .mileageLimit(policy.getMileageLimit())
                .conditions(policy.getConditions())
                .createdAt(policy.getCreatedAt())
                .build();
    }

    /**
     * Cập nhật thông tin Entity từ Request DTO.
     */
    public void updatePolicyFromRequest(WarrantyPolicy policy, WarrantyPolicyRequest request) {
        policy.setDurationMonths(request.getDurationMonths());
        policy.setMileageLimit(request.getMileageLimit());
        policy.setConditions(request.getConditions());
        // Không cập nhật partId vì chính sách thường gắn liền với một phụ tùng cố định
    }
}