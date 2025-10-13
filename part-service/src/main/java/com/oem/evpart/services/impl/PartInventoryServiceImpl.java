package com.oem.evpart.services.impl;

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

    @Override
    @Transactional
    public PartInventoryResponse updateInventoryStatus(Long inventoryId, String status) {
        PartInventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + inventoryId));

        // Thêm logic để validate `status` có hợp lệ không (ENUM('Available','Reserved','Defective'))
        inventory.setStatus(status); // Cần có validation cho Enum
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