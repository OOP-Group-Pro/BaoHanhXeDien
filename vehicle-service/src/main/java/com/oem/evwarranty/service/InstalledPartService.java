package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.request.InstalledPartRequestDTO;
import com.oem.evwarranty.dto.response.InstalledPartResponseDTO;
import com.oem.evwarranty.entity.InstalledPart;
import com.oem.evwarranty.entity.Vehicle;
import com.oem.evwarranty.exception.ResourceNotFoundException;
import com.oem.evwarranty.repository.InstalledPartRepository;
import com.oem.evwarranty.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Thêm Transactional

import com.oem.evwarranty.client.PartServiceClient;
import com.oem.evwarranty.dto.request.AllocationRequestDTO; // Import này vẫn đúng

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InstalledPartService {

    @Autowired
    private InstalledPartRepository installedPartRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private PartServiceClient partServiceClient;


    /**
     * Cập nhật: Thêm @Transactional và logic gọi Trừ kho (Allocate Stock).
     */
    @Transactional // Đảm bảo lưu và gọi trừ kho là một giao dịch
    public InstalledPartResponseDTO installPart(InstalledPartRequestDTO requestDTO) {
        Vehicle vehicle = vehicleRepository.findById(requestDTO.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + requestDTO.getVehicleId()));

        // --- BƯỚC 1: LƯU VÀO DB NỘI BỘ ---
        InstalledPart newPart = new InstalledPart();
        newPart.setPartId(requestDTO.getPartId());
        newPart.setSerialNumber(requestDTO.getSerialNumber());
        newPart.setInstallDate(requestDTO.getInstallDate());
        newPart.setStatus(requestDTO.getStatus());
        newPart.setVehicle(vehicle);

        InstalledPart savedPart = installedPartRepository.save(newPart);

        // --- BƯỚC 2: GỌI PART-SERVICE ĐỂ TRỪ KHO ---
        try {
            // Tạo DTO yêu cầu trừ kho
            AllocationRequestDTO allocationRequest = new AllocationRequestDTO(
                    savedPart.getPartId(),
                    1, // Mặc định lắp 1
                    requestDTO.getServiceCenterId(), // Lấy từ DTO input
                    null // Location (nếu có, không thì part-service tự suy)
            );

            // Gọi Feign Client
            log.info("Đang gọi part-service để trừ kho cho partId: {}", savedPart.getPartId());
            partServiceClient.allocateStock(allocationRequest);
            log.info("Trừ kho thành công.");

        } catch (Exception e) {
            // LỖI NGHIÊM TRỌNG:
            // Đã lưu part vào xe nhưng KHÔNG trừ được kho.
            // Cần log lại để hệ thống chạy bù (reconciliation).
            log.error("LỖI NGHIÊP VỤ: Không thể trừ kho cho partId {}. Cần chạy bù!", savedPart.getPartId(), e);

            // Tùy nghiệp vụ, bạn có thể "roll back" (xóa) bản ghi savedPart
            // hoặc để lại và báo lỗi cho hệ thống giám sát.
            // Ở đây chúng ta tạm log lỗi và vẫn trả về DTO.
        }

        return convertToDTO(savedPart);
    }


    public List<InstalledPartResponseDTO> getPartsByVehicleId(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
        }
        return installedPartRepository.findByVehicleVehicleId(vehicleId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void removePartFromVehicle(Long vehicleId, Long installedPartId) {
        InstalledPart part = installedPartRepository.findById(installedPartId)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("InstalledPart not found with ID: " + installedPartId);
                });

        if (!part.getVehicle().getVehicleId().equals(vehicleId)) {
            throw new ResourceNotFoundException("Part with id " + installedPartId + " does not belong to vehicle with id " + vehicleId);
        }
        installedPartRepository.deleteById(installedPartId);
    }

    private InstalledPartResponseDTO convertToDTO(InstalledPart part) {
        InstalledPartResponseDTO dto = new InstalledPartResponseDTO();
        dto.setInstalledId(part.getInstalledId());
        dto.setPartId(part.getPartId());
        dto.setSerialNumber(part.getSerialNumber());
        dto.setInstallDate(part.getInstallDate());
        dto.setStatus(part.getStatus());
        dto.setVehicleId(part.getVehicle().getVehicleId());
        return dto;
    }
}