package com.oem.evpart.services.impl;

import com.oem.evpart.dto.request.DecrementStockRequest;
import com.oem.evpart.dto.request.PartInventoryRequest;
import com.oem.evpart.dto.response.PartInventoryResponse;
import com.oem.evpart.exceptions.ResourceNotFoundException;
import com.oem.evpart.mappers.PartInventoryMapper; // Giả sử bạn đã tạo mapper này
import com.oem.evpart.models.Part;
import com.oem.evpart.models.PartInventory;
import com.oem.evpart.repositories.PartInventoryRepository;
import com.oem.evpart.repositories.PartRepository;
import com.oem.evpart.services.PartInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PartInventoryServiceImpl implements PartInventoryService {

    private final PartInventoryRepository inventoryRepository;
    private final PartRepository partRepository;
    private final PartInventoryMapper inventoryMapper;

    @Override
    @Transactional
    public PartInventoryResponse addOrUpdateStock(PartInventoryRequest request) {
        Part part = partRepository.findById(request.getPartId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + request.getPartId()));

        // Tìm kiếm tồn kho theo partId và location
        Optional<PartInventory> existingInventoryOpt = inventoryRepository.findByPart_PartIdAndLocation(request.getPartId(), request.getLocation());

        PartInventory inventory;
        if (existingInventoryOpt.isPresent()) {
            // Nếu đã tồn tại, cập nhật số lượng
            inventory = existingInventoryOpt.get();
            inventory.setQuantity(inventory.getQuantity() + request.getQuantity());
        } else {
            // Nếu chưa, tạo mới
            inventory = inventoryMapper.toPartInventory(request);
            inventory.setPart(part);
        }

        PartInventory savedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toPartInventoryResponse(savedInventory);
    }

    @Override
    @Transactional
    public PartInventoryResponse decrementStock(DecrementStockRequest request) {
        // 1. Tìm bản ghi tồn kho tương ứng với partId và location
        PartInventory inventory = inventoryRepository
                .findByPart_PartIdAndLocation(request.getPartId(), request.getLocation())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tồn kho cho Part ID "
                        + request.getPartId() + " tại địa điểm '" + request.getLocation() + "'."));

        // 2. Kiểm tra số lượng tồn
        Long currentQuantity = inventory.getQuantity();
        Long requestedQuantity = request.getQuantity();
        if (currentQuantity < requestedQuantity) {
            throw new IllegalArgumentException("Không đủ số lượng tồn kho tại '" + request.getLocation() +
                    "'. Còn lại: " + currentQuantity + ", Yêu cầu trừ: " + requestedQuantity);
        }

        // 3. Trừ số lượng và lưu lại
        inventory.setQuantity(currentQuantity - requestedQuantity);
        PartInventory updatedInventory = inventoryRepository.save(inventory);

        // 4. Trả về thông tin tồn kho sau khi đã trừ
        return inventoryMapper.toPartInventoryResponse(updatedInventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartInventoryResponse> getInventoryByPartId(Long partId) {
        return inventoryRepository.findByPart_PartId(partId).stream()
                .map(inventoryMapper::toPartInventoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartInventoryResponse> getInventoryByLocation(String location) {
        return inventoryRepository.findByLocation(location).stream()
                .map(inventoryMapper::toPartInventoryResponse)
                .collect(Collectors.toList());
    }

    // Trong file PartInventoryServiceImpl.java

    @Override
    @Transactional
    public PartInventoryResponse updateInventoryStatus(Long inventoryId, String status) {
        PartInventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + inventoryId));

        // FIX: Chuyển đổi String sang Enum một cách an toàn
        try {
            PartInventory.Status newStatus = PartInventory.Status.valueOf(status.trim()); // trim() để xóa khoảng trắng thừa
            inventory.setStatus(newStatus);
        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException("Invalid status value: '" + status + "'. Must be 'Available', 'Reserved', or 'Defective'.");
        }

        PartInventory updatedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toPartInventoryResponse(updatedInventory);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStockAvailable(Long partId, String location, int requiredQuantity) {
        return inventoryRepository.findByPart_PartIdAndLocation(partId, location)
                .map(inv -> inv.getQuantity() >= requiredQuantity && "Available".equals(inv.getStatus()))
                .orElse(false);
    }
}