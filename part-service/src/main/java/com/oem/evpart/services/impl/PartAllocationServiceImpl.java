package com.oem.evpart.services.impl;

import com.oem.evpart.dto.request.ClaimAllocationRequest;
import com.oem.evpart.dto.request.PartAllocationRequest;
import com.oem.evpart.dto.response.PartAllocationResponse;
import com.oem.evpart.exceptions.ResourceNotFoundException;
import com.oem.evpart.mappers.PartAllocationMapper;
import com.oem.evpart.mappers.PartInventoryMapper;
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

    @Override
    @Transactional // Rất quan trọng: Đảm bảo cả hai thao tác (trừ kho và tạo phiếu) thành công hoặc thất bại cùng nhau
    public PartAllocationResponse createAllocation(PartAllocationRequest request) {
        // 1. Tìm kho chứa phụ tùng
        PartInventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + request.getInventoryId()));

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
        newAllocation.setInventory(inventory); // Gán đối tượng kho đã tìm thấy

        PartAllocation savedAllocation = allocationRepository.save(newAllocation);

        return allocationMapper.toPartAllocationResponse(savedAllocation);
    }

    @Override
    @Transactional
    public PartAllocationResponse allocateForClaim(ClaimAllocationRequest request) {
        // Giả định: Lấy 1 partId đầu tiên từ danh sách Đạt gửi
        Long partId;
        try {
            partId = Long.parseLong(request.getPartNumbers().get(0));
        } catch (Exception e) {
            throw new IllegalArgumentException("partNumber không hợp lệ. Cần gửi partId dạng số.");
        }

        // 1. Tìm Part (để chắc chắn nó tồn tại)
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Phụ tùng (Part) với ID " + partId + " không tìm thấy."));

        // 2. LOGIC TÌM KHO: Sử dụng phương thức repository bạn đã chọn
        PartInventory inventory = inventoryRepository
                .findFirstByPartAndQuantityGreaterThanEqual(part, request.getQuantity())
                .orElseThrow(() -> new ResourceNotFoundException("Đã hết hàng cho phụ tùng " + part.getName()
                        + ". Yêu cầu " + request.getQuantity() + " cái."));

        // 3. Khi đã tìm được KHO (`inventoryId`), chúng ta gọi lại hàm `createAllocation` CŨ
        // để tái sử dụng logic (xác thực SC, trừ kho, tạo phiếu).

        PartAllocationRequest internalRequest = PartAllocationRequest.builder()
                .inventoryId(inventory.getInventoryId())
                .serviceCenterId(request.getServiceCenterId())
                .allocatedQty(request.getQuantity())
                .build();

        return createAllocation(internalRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public PartAllocationResponse getAllocationById(Long allocationId) {
        return allocationRepository.findById(allocationId)
                .map(allocationMapper::toPartAllocationResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found with id: " + allocationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartAllocationResponse> getAllocationsByServiceCenter(Integer serviceCenterId) {
        return allocationRepository.findByServiceCenterId(serviceCenterId).stream()
                .map(allocationMapper::toPartAllocationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartAllocationResponse> getAllocationsByInventoryId(Long inventoryId) {
        return allocationRepository.findByInventory_InventoryId(inventoryId).stream()
                .map(allocationMapper::toPartAllocationResponse)
                .collect(Collectors.toList());
    }
}