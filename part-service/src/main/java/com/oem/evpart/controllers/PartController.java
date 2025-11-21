package com.oem.evpart.controllers;

import com.oem.evpart.dto.request.ClaimAllocationRequest;
import com.oem.evpart.dto.request.PartRequest;
import com.oem.evpart.dto.response.PartAllocationResponse;
import com.oem.evpart.dto.response.PartResponse;
import com.oem.evpart.services.PartAllocationService;
import com.oem.evpart.services.PartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    private final PartAllocationService allocationService;

    @PostMapping
    public ResponseEntity<PartResponse> createPart(@Valid @RequestBody PartRequest partRequest) {
        PartResponse newPart = partService.createPart(partRequest);
        return new ResponseEntity<>(newPart, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartResponse> getPartById(@PathVariable Long id) {
        return ResponseEntity.ok(partService.getPartById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PartResponse>> getAllParts(
            @RequestParam(required = false) String name, // ⬇️ Nhận tham số name từ URL
            Pageable pageable
    ) {
        return ResponseEntity.ok(partService.getAllParts(name, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartResponse> updatePart(@PathVariable Long id, @Valid @RequestBody PartRequest partRequest) {
        return ResponseEntity.ok(partService.updatePart(id, partRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePart(@PathVariable Long id) {
        partService.deletePart(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * API này dành cho Đạt (Warranty-Service) gọi.
     * Nhận yêu cầu cấp phát dựa trên thông tin claim (tự động tìm kho).
     */
    @PostMapping("/allocate-claim")
    public ResponseEntity<PartAllocationResponse> requestPartAllocationForClaim(
            @Valid @RequestBody ClaimAllocationRequest request) {

        PartAllocationResponse response = allocationService.allocateForClaim(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/by-numbers")
    public ResponseEntity<Map<String, PartResponse>> getPartsByNumbers(
            @RequestBody List<String> partNumbers
    ) {
        // Bạn sẽ cần implement hàm 'getPartDetailsByNumbers' trong PartService
        // Nó nhận 1 List<String> và trả về Map<String (partNumber), PartResponse (chi tiết)>
        Map<String, PartResponse> partMap = partService.getPartDetailsByNumbers(partNumbers);
        return ResponseEntity.ok(partMap);
    }
}