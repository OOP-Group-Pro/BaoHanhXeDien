package com.oem.evpart.services;

import com.oem.evpart.dto.request.WarrantyPolicyRequest;
import com.oem.evpart.dto.response.WarrantyPolicyResponse;
import java.util.List;

public interface WarrantyPolicyService {
    WarrantyPolicyResponse createPolicy(WarrantyPolicyRequest request);
    WarrantyPolicyResponse getPolicyById(Long policyId);
    List<WarrantyPolicyResponse> getPoliciesByPartId(Long partId);
    WarrantyPolicyResponse updatePolicy(Long policyId, WarrantyPolicyRequest request);
    void deletePolicy(Long policyId);
}