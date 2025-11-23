package com.oem.evpart.controllers;

import com.oem.evpart.dto.request.DecrementStockRequest;
import com.oem.evpart.dto.request.PartAllocationRequest;
import com.oem.evpart.dto.response.PartAllocationResponse;
import com.oem.evpart.dto.response.PartAllocationStatusDto;
import com.oem.evpart.dto.response.PartInventoryResponse;
import com.oem.evpart.services.PartAllocationService;
import com.oem.evpart.services.PartInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/allocations")
@RequiredArgsConstructor
public class PartAllocationController {

    private final PartAllocationService allocationService;

    private final PartInventoryService inventoryService;
    @PostMapping
    public ResponseEntity<PartAllocationResponse> createAllocation(@Valid @RequestBody PartAllocationRequest request) {
        PartAllocationResponse response = allocationService.createAllocation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // --- API MỚI ĐỂ TRỪ KHO SAU KHI SỬ DỤNG ---
    /**
     * Endpoint này được gọi bởi service khác (vd: VehicleService)
     * sau khi một phụ tùng đã được lắp đặt thành công, để trừ tồn kho.
     */
    @PostMapping("/decrement")
    public ResponseEntity<PartInventoryResponse> decrementStockAfterUsage(
            @Valid @RequestBody DecrementStockRequest request) {
        return ResponseEntity.ok(inventoryService.decrementStock(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartAllocationResponse> getAllocationById(@PathVariable Long id) {
        return ResponseEntity.ok(allocationService.getAllocationById(id));
    }

    @GetMapping("/service-center/{centerId}")
    public ResponseEntity<List<PartAllocationResponse>> getAllocationsByServiceCenter(@PathVariable Integer centerId) {
        return ResponseEntity.ok(allocationService.getAllocationsByServiceCenter(centerId));
    }

    @GetMapping("/inventory/{inventoryId}")
    public ResponseEntity<List<PartAllocationResponse>> getAllocationsByInventory(@PathVariable Long inventoryId) {
        return ResponseEntity.ok(allocationService.getAllocationsByInventoryId(inventoryId));
    }


    /**
     * GET /api/v1/allocations/status-by-claim/{claimCode}
     * Chức năng: Lấy trạng thái cấp phát phụ tùng cho một Claim ID cụ thể.
     * API này được gọi bởi Giao diện Kỹ thuật viên để hiển thị cột trạng thái.
     */
    @GetMapping("/status-by-claim/{claimCode}")
    public ResponseEntity<PartAllocationStatusDto> getAllocationStatusForClaim(
            @PathVariable String claimCode
    ) {
        // Service sẽ chứa logic tìm kiếm allocation theo claimCode và trả về DTO
        PartAllocationStatusDto statusDto = allocationService.getStatusByClaimCode(claimCode);
        return ResponseEntity.ok(statusDto);
    }
}