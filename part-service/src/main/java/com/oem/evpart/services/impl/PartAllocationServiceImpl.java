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

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public List<PartAllocationResponse> allocateForClaim(ClaimAllocationRequest request) {

        List<PartAllocationResponse> responses = new ArrayList<>();

        // 1. Duyệt qua từng Item (Mã + Số lượng)
        for (ClaimAllocationRequest.AllocationItem item : request.getItems()) {

            String partSku = item.getPartNumber();
            int qtyRequired = item.getQuantity(); // Số lượng riêng của từng món

            // 2. Tìm Part theo Mã SKU (serialNumber trong DB Part)
            Part part = partRepository.findBySerialNumber(partSku)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy phụ tùng với mã SKU: " + partSku));

            // 3. Tìm kho còn hàng (logic cũ của bạn)
            PartInventory inventory = inventoryRepository
                    .findFirstByPartAndQuantityGreaterThanEqual(part, (long) qtyRequired)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Hết hàng: " + part.getName() + " (Cần: " + qtyRequired + ")"));

            // 4. Trừ kho
            inventory.setQuantity(inventory.getQuantity() - qtyRequired);
            inventoryRepository.save(inventory);

            // 5. Tạo bản ghi Allocation
            PartAllocation allocation = PartAllocation.builder()
                    .inventory(inventory)
                    .claimCode(request.getClaimCode()) // Lưu ý: Kiểm tra DB dùng claimId (Long) hay claimCode (String)
                    // Nếu DB dùng claimId (Long), bạn phải sửa DTO hoặc Entity cho khớp
                    .deliveredQty((long) qtyRequired)      // ⬅️ Lưu số lượng thực tế
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
    public PartAllocationStatusDto getStatusByClaimCode(String claimCode) {
        // 1. Tìm allocation theo claimCode (giả định bạn có field claimCode trong entity)
        List<PartAllocation> allocations = allocationRepository.findByClaimCode(claimCode);

        if (allocations.isEmpty()) {
            // Không có allocation => có thể không yêu cầu phụ tùng
            return new PartAllocationStatusDto(claimCode, PartAllocation.AllocationStatus.NOT_REQUIRED);
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

        return new PartAllocationStatusDto(claimCode, status);
    }
}
