package com.oem.evpart.controllers;

import com.oem.evpart.dto.request.WarrantyPolicyRequest;
import com.oem.evpart.dto.response.WarrantyPolicyResponse;
import com.oem.evpart.services.WarrantyPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warranty-policies")
@RequiredArgsConstructor
public class WarrantyPolicyController {

    private final WarrantyPolicyService policyService;

    @PostMapping
    public ResponseEntity<WarrantyPolicyResponse> createPolicy(@Valid @RequestBody WarrantyPolicyRequest request) {
        WarrantyPolicyResponse newPolicy = policyService.createPolicy(request);
        return new ResponseEntity<>(newPolicy, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarrantyPolicyResponse> getPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }

    @GetMapping("/part/{partId}")
    public ResponseEntity<List<WarrantyPolicyResponse>> getPoliciesByPartId(@PathVariable Long partId) {
        return ResponseEntity.ok(policyService.getPoliciesByPartId(partId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarrantyPolicyResponse> updatePolicy(@PathVariable Long id, @Valid @RequestBody WarrantyPolicyRequest request) {
        return ResponseEntity.ok(policyService.updatePolicy(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }
}