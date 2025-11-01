package com.oem.evpart.services.impl;

import com.oem.evpart.dto.request.WarrantyPolicyRequest;
import com.oem.evpart.dto.response.WarrantyPolicyResponse;
import com.oem.evpart.exceptions.ResourceNotFoundException;
import com.oem.evpart.mappers.WarrantyPolicyMapper;
import com.oem.evpart.models.Part;
import com.oem.evpart.models.WarrantyPolicy;
import com.oem.evpart.repositories.PartRepository;
import com.oem.evpart.repositories.WarrantyPolicyRepository;
import com.oem.evpart.services.WarrantyPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class WarrantyPolicyServiceImpl implements WarrantyPolicyService {

    private final WarrantyPolicyRepository policyRepository;
    private final PartRepository partRepository;
    private final WarrantyPolicyMapper policyMapper; // Giả sử bạn đã tạo mapper này

    @Override
    @Transactional
    public WarrantyPolicyResponse createPolicy(WarrantyPolicyRequest request) {
        Part part = partRepository.findById(request.getPartId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + request.getPartId()));

        WarrantyPolicy newPolicy = policyMapper.toWarrantyPolicy(request);
        newPolicy.setPart(part);

        WarrantyPolicy savedPolicy = policyRepository.save(newPolicy);
        return policyMapper.toWarrantyPolicyResponse(savedPolicy);
    }

    @Override
    @Transactional(readOnly = true)
    public WarrantyPolicyResponse getPolicyById(Long policyId) {
        WarrantyPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + policyId));
        return policyMapper.toWarrantyPolicyResponse(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarrantyPolicyResponse> getPoliciesByPartId(Long partId) {
        if (!partRepository.existsById(partId)) {
            throw new ResourceNotFoundException("Part not found with id: " + partId);
        }
        List<WarrantyPolicy> policies = policyRepository.findByPart_PartId(partId);
        return policies.stream()
                .map(policyMapper::toWarrantyPolicyResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WarrantyPolicyResponse updatePolicy(Long policyId, WarrantyPolicyRequest request) {
        WarrantyPolicy existingPolicy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + policyId));

        policyMapper.updatePolicyFromRequest(existingPolicy, request);
        WarrantyPolicy updatedPolicy = policyRepository.save(existingPolicy);
        return policyMapper.toWarrantyPolicyResponse(updatedPolicy);
    }

    @Override
    @Transactional
    public void deletePolicy(Long policyId) {
        if (!policyRepository.existsById(policyId)) {
            throw new ResourceNotFoundException("Policy not found with id: " + policyId);
        }
        policyRepository.deleteById(policyId);
    }
}