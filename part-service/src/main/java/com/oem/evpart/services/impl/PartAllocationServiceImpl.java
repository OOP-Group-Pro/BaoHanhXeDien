package com.oem.evpart.services.impl;

import com.oem.evpart.dto.request.ClaimAllocationRequest;
import com.oem.evpart.dto.request.CompleteAllocationRequest;
import com.oem.evpart.dto.request.PartAllocationRequest;
import com.oem.evpart.dto.response.PageCacheDto;
import com.oem.evpart.dto.response.PartAllocationResponse;
import com.oem.evpart.dto.response.PartAllocationStatusDto;

import com.oem.evpart.exceptions.ResourceNotFoundException;
import com.oem.evpart.mappers.PartAllocationMapper;
import com.oem.evpart.mappers.PartMapper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    private final PartMapper partMapper;
    private final PartAllocationMapper partAllocationMapper;

    // ---------------- CREATE ALLOCATION (THỦ CÔNG) ----------------
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "allocation_status", allEntries = true),
            @CacheEvict(value = "allocations_by_inventory", key = "#request.inventoryId"),
            // Lưu ý: key cache phải khớp tên tham số, ở đây là request
            // Nếu lỗi Spring Expression, dùng allEntries=true cho an toàn
            @CacheEvict(value = "allocations", allEntries = true)
    })
    public PartAllocationResponse createAllocation(PartAllocationRequest request) {
        // 1. Tìm & Trừ kho (Giữ nguyên logic cũ của bạn)
        PartInventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with id: " + request.getInventoryId()));

        if (inventory.getQuantity() < request.getAllocatedQty()) {
            throw new IllegalArgumentException("Insufficient stock.");
        }

        inventory.setQuantity(inventory.getQuantity() - request.getAllocatedQty());
        inventoryRepository.save(inventory);

        // 2. Tạo Allocation
        PartAllocation newAllocation = allocationMapper.toPartAllocation(request);
        newAllocation.setInventory(inventory);
        newAllocation.setAllocatedDate(LocalDateTime.now());

        // ⚠️ FIX QUAN TRỌNG: Set giá trị mặc định cho các trường bắt buộc
        // Vì tạo thủ công nên không có Claim Code thật
        newAllocation.setClaimCode("MANUAL_ALLOCATION");
        // Tạo thủ công thì coi như đã sẵn sàng giao
        newAllocation.setStatus(PartAllocation.AllocationStatus.READY_TO_INSTALL);

        PartAllocation savedAllocation = allocationRepository.save(newAllocation);

        log.info("Created manual allocation id={}", savedAllocation.getAllocationId());
        return allocationMapper.toPartAllocationResponse(savedAllocation);
    }

    // ---------------- ALLOCATE FOR CLAIM ----------------
    @Override
    @Transactional
    @CacheEvict(value = "allocation_status", allEntries = true) // Xóa cache liên quan
    public List<PartAllocationResponse> allocateForClaim(ClaimAllocationRequest request) {
        String targetLocation = String.valueOf(request.getServiceCenterId());

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

            // 2. THUẬT TOÁN CHỌN KHO (Priority Check)

            // Ưu tiên 1: Tìm trong chính kho của Trạm đó (targetLocation)
            for (Part part : candidateParts) {
                var invOpt = inventoryRepository.findByPartAndLocation(part, targetLocation);
                if (invOpt.isPresent() && invOpt.get().getQuantity() >= qtyRequired) {
                    selectedInventory = invOpt.get();
                    break;
                }
            }

            // 🔥 THÊM ĐOẠN NÀY: Ưu tiên 2 (Tìm bất kỳ kho nào còn hàng nếu kho trạm hết)
            if (selectedInventory == null) {
                for (Part part : candidateParts) {
                    // Tìm kho nào đó có đủ hàng (bỏ qua điều kiện location)
                    var invOpt = inventoryRepository.findFirstByPartAndQuantityGreaterThanEqual(part, (long) qtyRequired);
                    if (invOpt.isPresent()) {
                        selectedInventory = invOpt.get();
                        break;
                    }
                }
            }
            // ---------------------------------------------------------

            if (selectedInventory == null) {
                throw new ResourceNotFoundException("Hết hàng (Out of Stock) cho loại: " + reqPartType);
            }

            selectedInventory.setQuantity(selectedInventory.getQuantity() - qtyRequired);
            inventoryRepository.save(selectedInventory);

            PartAllocation allocation = PartAllocation.builder()
                    .inventory(selectedInventory)
                    .claimCode(request.getClaimCode())
                    .allocatedQty((long) qtyRequired)
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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value="allocations")
    public PageCacheDto<PartAllocationResponse> getAllAllocations (Pageable pageable) {
        Page<PartAllocation> allocations = allocationRepository.findAll(pageable);
        return PageCacheDto.from(allocations.map(partAllocationMapper::toPartAllocationResponse));
    }

    // Trong Impl
    @Override
    @Transactional
    // Evict cả cache kho để Admin thấy hàng lỗi tăng lên ngay lập tức
    @CacheEvict(value = {"allocation_status", "inventory_list", "allocations"}, allEntries = true)
    public void completeAllocation(CompleteAllocationRequest request) {
        // --- PHẦN 1: CHỐT XUẤT KHO (Cũ) ---
        List<PartAllocation> allocations = allocationRepository.findByClaimCode(request.getClaimCode());

        // Lấy Service Center ID từ allocation cũ để biết trả về kho nào
        Long serviceCenterId = null;

        for (PartAllocation allocation : allocations) {
            if (allocation.getStatus() != PartAllocation.AllocationStatus.COMPLETED) {
                allocation.setStatus(PartAllocation.AllocationStatus.COMPLETED);
                allocationRepository.save(allocation);
                serviceCenterId = allocation.getServiceCenterId(); // Lưu lại ID trạm
            }
        }

        // --- PHẦN 2: NHẬP KHO HÀNG LỖI (MỚI) ---
        if (request.getReturnedParts() != null && !request.getReturnedParts().isEmpty() && serviceCenterId != null) {

            String locationCode = String.valueOf(serviceCenterId); // VD: "101"

            for (CompleteAllocationRequest.ReturnedPart returnItem : request.getReturnedParts()) {
                // A. Tìm thông tin Part (Metadata)
                // Giả sử ta lấy Part đầu tiên tìm thấy theo Type (Vì trong kho quản lý theo Type)
                List<Part> parts = partRepository.findAllByPartType(returnItem.getPartType());
                if (parts.isEmpty()) continue;
                Part partMeta = parts.get(0);

                // B. Tìm kho hàng lỗi của trạm này
                // Tìm dòng kho: Part = PIN, Location = 101, Status = DEFECTIVE
                var defectiveInventoryOpt = inventoryRepository.findAll().stream()
                        .filter(i -> i.getPart().getPartType().equals(returnItem.getPartType())
                                && i.getLocation().equals(locationCode)
                                && i.getStatus() == PartInventory.Status.Defective)
                        .findFirst();

                PartInventory defectiveInventory;

                if (defectiveInventoryOpt.isPresent()) {
                    // C.1: Nếu đã có dòng hàng lỗi -> Cộng dồn
                    defectiveInventory = defectiveInventoryOpt.get();
                    defectiveInventory.setQuantity(defectiveInventory.getQuantity() + returnItem.getQuantity());
                } else {
                    // C.2: Nếu chưa có -> Tạo dòng mới (Status = DEFECTIVE)
                    defectiveInventory = PartInventory.builder()
                            .part(partMeta)
                            .location(locationCode)
                            .quantity((long) returnItem.getQuantity())
                            .status(PartInventory.Status.Defective) // 🔴 QUAN TRỌNG
                            .build();
                }

                // D. Lưu kho hàng lỗi
                inventoryRepository.save(defectiveInventory);
            }
        }
    }
}
