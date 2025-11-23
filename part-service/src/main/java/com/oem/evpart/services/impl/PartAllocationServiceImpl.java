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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
    @Caching(evict = {
            @CacheEvict(value = "allocation_status", allEntries = true),
            // SỬA: Cần xóa cache danh sách allocation của kho
            @CacheEvict(value = "allocations_by_inventory", key = "#request.inventoryId"),
            @CacheEvict(value = "allocations_by_center", key = "#request.serviceCenterId")
    })
    public PartAllocationResponse createAllocation(PartAllocationRequest request) {
        PartInventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with id: " + request.getInventoryId()));

        if (inventory.getQuantity() < request.getAllocatedQty()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + inventory.getQuantity()
                    + ", Requested: " + request.getAllocatedQty());
        }

        inventory.setQuantity(inventory.getQuantity() - request.getAllocatedQty());
        inventoryRepository.save(inventory);

        PartAllocation newAllocation = allocationMapper.toPartAllocation(request);
        newAllocation.setInventory(inventory);

        PartAllocation savedAllocation = allocationRepository.save(newAllocation);
        log.info("Created allocation id={} inventoryId={}", savedAllocation.getAllocationId(), request.getInventoryId());
        return allocationMapper.toPartAllocationResponse(savedAllocation);
    }

    // ---------------- ALLOCATE FOR CLAIM ----------------
    @Override
    @Transactional
    @CacheEvict(value = "allocation_status", allEntries = true) // Xóa cache liên quan
    public List<PartAllocationResponse> allocateForClaim(ClaimAllocationRequest request) {
        List<PartAllocationResponse> responses = new ArrayList<>();

        // 1. Duyệt qua từng Item yêu cầu
        for (ClaimAllocationRequest.AllocationItem item : request.getItems()) {

            // ⚠️ QUAN TRỌNG: Giá trị này là "PIN", "MOTOR" (Mã Loại)
            String reqPartType = item.getPartNumber();
            int qtyRequired = item.getQuantity();

            // 2. Tìm danh sách các Part Model thuộc loại này (VD: Tìm tất cả các loại PIN)
            List<Part> candidateParts = partRepository.findAllByPartType(reqPartType);

            if (candidateParts.isEmpty()) {
                throw new ResourceNotFoundException("Không tìm thấy loại phụ tùng nào có mã loại: " + reqPartType);
            }

            // 3. Thuật toán tìm kho: "First Match" (Tìm thằng nào còn hàng thì lấy)
            PartInventory selectedInventory = null;
            Part selectedPart = null;

            for (Part part : candidateParts) {
                // Tìm trong kho của Part này xem có đủ số lượng không
                // Lưu ý: Cần filter thêm serviceCenterId nếu kho của bạn chia theo trạm
                // Ở đây tôi giả định kho chung hoặc logic tìm inventory đã bao gồm location
                var inventoryOpt = inventoryRepository.findFirstByPartAndQuantityGreaterThanEqual(part, (long) qtyRequired);

                if (inventoryOpt.isPresent()) {
                    selectedInventory = inventoryOpt.get();
                    selectedPart = part;
                    break;
                }
            }

            if (selectedInventory == null) {
                throw new ResourceNotFoundException("Hết hàng (Out of Stock) cho loại: " + reqPartType);
            }

            selectedInventory.setQuantity(selectedInventory.getQuantity() - qtyRequired);
            inventoryRepository.save(selectedInventory);

            PartAllocation allocation = PartAllocation.builder()
                    .inventory(selectedInventory)
                    .claimCode(request.getClaimCode())
                    .deliveredQty((long) qtyRequired)
                    .allocatedDate(LocalDateTime.now())
                    .status(PartAllocation.AllocationStatus.PENDING)
                    .serviceCenterId(request.getServiceCenterId())
                    .build();

            PartAllocation savedAllocation = allocationRepository.save(allocation);
            responses.add(allocationMapper.toPartAllocationResponse(savedAllocation));
        }

        return responses;
    }

    // ---------------- GET ALLOCATION BY ID ----------------
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "allocations_by_id", key = "#allocationId")
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
    @Cacheable(value = "allocations_by_inventory", key = "#inventoryId")
    public List<PartAllocationResponse> getAllocationsByInventoryId(Long inventoryId) {
        return allocationRepository.findByInventory_InventoryId(inventoryId).stream()
                .map(allocationMapper::toPartAllocationResponse)
                .collect(Collectors.toList());
    }

    // ---------------- NEW: GET STATUS BY CLAIM ID ----------------
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "allocation_status", key = "#claimCode")
    public PartAllocationStatusDto getStatusByClaimCode(String claimCode) {
        List<PartAllocation> allocations = allocationRepository.findByClaimCode(claimCode);

        if (allocations.isEmpty()) {
            return new PartAllocationStatusDto(claimCode, PartAllocation.AllocationStatus.NOT_REQUIRED);
        }

        PartAllocation allocation = allocations.get(0);

        PartAllocation.AllocationStatus status;

        if (allocation.getDeliveredQty() == null || allocation.getDeliveredQty() == 0) {
            status = PartAllocation.AllocationStatus.WAITING_FOR_PART;
        } else if (allocation.getDeliveredQty() > 0 && allocation.getDeliveredQty() < allocation.getAllocatedQty()) {
            status = PartAllocation.AllocationStatus.PENDING;
        } else {
            status = PartAllocation.AllocationStatus.READY_TO_INSTALL;
        }

        return new PartAllocationStatusDto(claimCode, status);
    }
}
