package com.oem.evpart.controllers;

import com.oem.evpart.dto.request.PartInventoryRequest;
import com.oem.evpart.dto.request.UpdateInventoryStatusRequest; // Bạn cần tạo DTO này
import com.oem.evpart.dto.request.UpdateQuantityRequest;
import com.oem.evpart.dto.response.PartInventoryResponse;
import com.oem.evpart.services.PartInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class PartInventoryController {

    private final PartInventoryService inventoryService;

    @GetMapping("/all-stock")
    public ResponseEntity<List<PartInventoryResponse>> getAllStock() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @PostMapping("/stock")
    public ResponseEntity<PartInventoryResponse> addOrUpdateStock(@Valid @RequestBody PartInventoryRequest request) {
        return ResponseEntity.ok(inventoryService.addOrUpdateStock(request));
    }

    @GetMapping("/part/{partId}")
    public ResponseEntity<List<PartInventoryResponse>> getInventoryByPartId(@PathVariable Long partId) {
        return ResponseEntity.ok(inventoryService.getInventoryByPartId(partId));
    }

    @GetMapping("/location")
    public ResponseEntity<List<PartInventoryResponse>> getInventoryByLocation(@RequestParam String location) {
        return ResponseEntity.ok(inventoryService.getInventoryByLocation(location));
    }

    // Dùng PATCH vì đây là cập nhật một phần của resource
    @PatchMapping("/{inventoryId}/status")
    public ResponseEntity<PartInventoryResponse> updateStatus(@PathVariable Long inventoryId, @Valid @RequestBody UpdateInventoryStatusRequest request) {
        return ResponseEntity.ok(inventoryService.updateInventoryStatus(inventoryId, request.getStatus()));
    }
    @PatchMapping("/{inventoryId}/quantity")
    public ResponseEntity<PartInventoryResponse> updateInventoryQuantity(
            @PathVariable Long inventoryId,
            @Valid @RequestBody UpdateQuantityRequest request) {

        PartInventoryResponse updatedInventory = inventoryService.updateInventoryQuantity(
                inventoryId,
                request.getQuantity()
        );
        return ResponseEntity.ok(updatedInventory);
    }
}

