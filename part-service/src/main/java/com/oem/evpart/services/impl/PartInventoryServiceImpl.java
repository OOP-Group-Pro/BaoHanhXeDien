package com.oem.evpart.services.impl;

import com.oem.evpart.dto.request.DecrementStockRequest;
import com.oem.evpart.dto.request.PartInventoryRequest;
import com.oem.evpart.dto.response.PartInventoryResponse;
import com.oem.evpart.exceptions.ResourceNotFoundException;
import com.oem.evpart.mappers.PartInventoryMapper;
import com.oem.evpart.models.Part;
import com.oem.evpart.models.PartInventory;
import com.oem.evpart.repositories.PartInventoryRepository;
import com.oem.evpart.repositories.PartRepository;
import com.oem.evpart.services.PartInventoryService;
import com.oem.evpart.services.PartService; // [MỚI] Import PartService
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartInventoryServiceImpl implements PartInventoryService {

    private final PartInventoryRepository inventoryRepository;
    private final PartRepository partRepository;
    private final PartInventoryMapper inventoryMapper;
    
    // [MỚI] Inject PartService để gọi logic kiểm tra RabbitMQ
    private final PartService partService;

    @Override
    @Transactional
    @CacheEvict(value = { "inventory_part", "inventory_location" }, allEntries = true)
    public PartInventoryResponse addOrUpdateStock(PartInventoryRequest request) {
        Part part = partRepository.findById(request.getPartId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + request.getPartId()));

        Optional<PartInventory> existingInventoryOpt = inventoryRepository.findByPart_PartIdAndLocation(request.getPartId(), request.getLocation());

        PartInventory inventory;
        if (existingInventoryOpt.isPresent()) {
            inventory = existingInventoryOpt.get();
            inventory.setQuantity(inventory.getQuantity() + request.getQuantity());
        } else {
            inventory = inventoryMapper.toPartInventory(request);
            inventory.setPart(part);
        }

        PartInventory savedInventory = inventoryRepository.save(inventory);
        log.info("addOrUpdateStock - partId={}, location={}, qty={}", request.getPartId(), request.getLocation(), request.getQuantity());
        return inventoryMapper.toPartInventoryResponse(savedInventory);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "inventory_part", key = "#request.partId"), 
            @CacheEvict(value = "inventory_location", key = "#request.location") 
    })
    public PartInventoryResponse decrementStock(DecrementStockRequest request) {
        // 1. Tìm Inventory
        PartInventory inventory = inventoryRepository
                .findByPart_PartIdAndLocation(request.getPartId(), request.getLocation())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tồn kho cho Part ID "
                        + request.getPartId() + " tại địa điểm '" + request.getLocation() + "'."));

        // 2. Kiểm tra số lượng
        Long currentQuantity = inventory.getQuantity();
        Long requestedQuantity = request.getQuantity();
        if (currentQuantity < requestedQuantity) {
            throw new IllegalArgumentException("Không đủ số lượng tồn kho tại '" + request.getLocation() +
                    "'. Còn lại: " + currentQuantity + ", Yêu cầu trừ: " + requestedQuantity);
        }

        // 3. Trừ kho và Lưu
        inventory.setQuantity(currentQuantity - requestedQuantity);
        PartInventory updatedInventory = inventoryRepository.save(inventory);
        
        log.info("decrementStock - partId={}, location={}, deducted={}", request.getPartId(), request.getLocation(), requestedQuantity);

        // [MỚI - QUAN TRỌNG] 4. Gọi PartService để kiểm tra tổng tồn kho và bắn RabbitMQ
        try {
            // Lấy Serial Number từ Part liên kết
            String serialNumber = updatedInventory.getPart().getSerialNumber();
            
            // Gọi hàm check (Fire & Forget logic bên trong)
            partService.checkStockAndNotify(serialNumber);
            
        } catch (Exception e) {
            // Log lỗi nhưng KHÔNG rollback transaction DB (Vì trừ kho đã thành công)
            log.error("⚠️ Lỗi khi kiểm tra cảnh báo tồn kho RabbitMQ: {}", e.getMessage());
        }

        return inventoryMapper.toPartInventoryResponse(updatedInventory);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "inventory_part", key = "#partId")
    public List<PartInventoryResponse> getInventoryByPartId(Long partId) {
        return inventoryRepository.findByPart_PartId(partId).stream()
                .map(inventoryMapper::toPartInventoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "inventory_location", key = "#location")
    public List<PartInventoryResponse> getInventoryByLocation(String location) {
        return inventoryRepository.findByLocation(location).stream()
                .map(inventoryMapper::toPartInventoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CachePut(value = "inventory_part", key = "#inventory.part.partId")
    @CacheEvict(value = "inventory_location", allEntries = true)
    public PartInventoryResponse updateInventoryStatus(Long inventoryId, String status) {
        PartInventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + inventoryId));

        try {
            PartInventory.Status newStatus = PartInventory.Status.valueOf(status.trim());
            inventory.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: '" + status + "'. Must be 'Available', 'Reserved', or 'Defective'.");
        }

        PartInventory updatedInventory = inventoryRepository.save(inventory);
        log.info("updateInventoryStatus - inventoryId={}, newStatus={}", inventoryId, status);
        return inventoryMapper.toPartInventoryResponse(updatedInventory);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStockAvailable(Long partId, String location, int requiredQuantity) {
        return inventoryRepository.findByPart_PartIdAndLocation(partId, location)
                .map(inv -> inv.getQuantity() >= requiredQuantity && PartInventory.Status.Available.name().equals(inv.getStatus().name()))
                .orElse(false);
    }
}