package com.oem.evpart.services.impl;

import com.oem.evpart.dto.request.ClaimAllocationRequest;
import com.oem.evpart.dto.request.PartAllocationRequest;
import com.oem.evpart.dto.response.PartAllocationResponse;
import com.oem.evpart.dto.response.PartAllocationStatusDto;

import com.oem.evpart.exceptions.ResourceNotFoundException;
import com.oem.evpart.mappers.PartAllocationMapper;
import com.oem.evpart.models.Part;
import com.oem.evpart.models.PartAllocation;
import com.oem.evpart.models.PartInventory;
import com.oem.evpart.repositories.PartAllocationRepository;
import com.oem.evpart.repositories.PartInventoryRepository;
import com.oem.evpart.repositories.PartRepository;
import com.oem.evpart.services.PartAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartAllocationServiceImpl implements PartAllocationService {

    private final PartAllocationRepository allocationRepository;
    private final PartInventoryRepository inventoryRepository;
    private final PartAllocationMapper allocationMapper;
    private final PartRepository partRepository;

    // ---------------- CREATE ALLOCATION ----------------
    @Override
    @Transactional
    public PartAllocationResponse createAllocation(PartAllocationRequest request) {
        // 1. Tìm kho chứa phụ tùng
        PartInventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with id: " + request.getInventoryId()));

        // 2. Kiểm tra số lượng tồn kho
        if (inventory.getQuantity() < request.getAllocatedQty()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + inventory.getQuantity()
                    + ", Requested: " + request.getAllocatedQty());
        }

        // 3. Trừ số lượng tồn kho
        inventory.setQuantity(inventory.getQuantity() - request.getAllocatedQty());
        inventoryRepository.save(inventory);

        // 4. Tạo bản ghi phân bổ
        PartAllocation newAllocation = allocationMapper.toPartAllocation(request);
        newAllocation.setInventory(inventory);

        PartAllocation savedAllocation = allocationRepository.save(newAllocation);
        return allocationMapper.toPartAllocationResponse(savedAllocation);
    }

    // ---------------- ALLOCATE FOR CLAIM ----------------
    @Override
    @Transactional
    public PartAllocationResponse allocateForClaim(ClaimAllocationRequest request) {
        Long partId;
        try {
            partId = Long.parseLong(request.getPartNumbers().get(0));
        } catch (Exception e) {
            throw new IllegalArgumentException("partNumber không hợp lệ. Cần gửi partId dạng số.");
        }

        // 1. Tìm Part
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Phụ tùng (Part) với ID " + partId + " không tìm thấy."));

        // 2. Tìm kho còn hàng
        PartInventory inventory = inventoryRepository
                .findFirstByPartAndQuantityGreaterThanEqual(part, request.getQuantity())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Đã hết hàng cho phụ tùng " + part.getName()
                                + ". Yêu cầu " + request.getQuantity() + " cái."));

        // 3. Gọi lại hàm createAllocation
        PartAllocationRequest internalRequest = PartAllocationRequest.builder()
                .inventoryId(inventory.getInventoryId())
                .serviceCenterId(request.getServiceCenterId())
                .allocatedQty(request.getQuantity())
                .build();

        return createAllocation(internalRequest);
    }

    // ---------------- GET ALLOCATION BY ID ----------------
    @Override
    @Transactional(readOnly = true)
    public PartAllocationResponse getAllocationById(Long allocationId) {
        return allocationRepository.findById(allocationId)
                .map(allocationMapper::toPartAllocationResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Allocation not found with id: " + allocationId));
    }

    // ---------------- GET ALLOCATION BY SERVICE CENTER ----------------
    @Override
    @Transactional(readOnly = true)
    public List<PartAllocationResponse> getAllocationsByServiceCenter(Integer serviceCenterId) {
        return allocationRepository.findByServiceCenterId(serviceCenterId).stream()
                .map(allocationMapper::toPartAllocationResponse)
                .collect(Collectors.toList());
    }

    // ---------------- GET ALLOCATION BY INVENTORY ----------------
    @Override
    @Transactional(readOnly = true)
    public List<PartAllocationResponse> getAllocationsByInventoryId(Long inventoryId) {
        return allocationRepository.findByInventory_InventoryId(inventoryId).stream()
                .map(allocationMapper::toPartAllocationResponse)
                .collect(Collectors.toList());
    }

    // ---------------- NEW: GET STATUS BY CLAIM ID ----------------
    @Override
    @Transactional(readOnly = true)
    public PartAllocationStatusDto getStatusByClaimId(Long claimId) {
        // 1. Tìm allocation theo claimId (giả định bạn có field claimId trong entity)
        List<PartAllocation> allocations = allocationRepository.findByClaimId(claimId);

        if (allocations.isEmpty()) {
            // Không có allocation => có thể không yêu cầu phụ tùng
            return new PartAllocationStatusDto(claimId, PartAllocation.AllocationStatus.NOT_REQUIRED);
        }

        // 2. Lấy allocation gần nhất (hoặc đầu tiên)
        PartAllocation allocation = allocations.get(0);

        // 3. Xác định trạng thái (giả định entity có trường "status" hoặc "deliveredQty")
        PartAllocation.AllocationStatus status;

        if (allocation.getDeliveredQty() == null || allocation.getDeliveredQty() == 0) {
            status = PartAllocation.AllocationStatus.WAITING_FOR_PART;
        } else if (allocation.getDeliveredQty() > 0 && allocation.getDeliveredQty() < allocation.getAllocatedQty()) {
            status = PartAllocation.AllocationStatus.PENDING;
        } else {
            status = PartAllocation.AllocationStatus.READY_TO_INSTALL;
        }

        return new PartAllocationStatusDto(claimId, status);
    }
}
