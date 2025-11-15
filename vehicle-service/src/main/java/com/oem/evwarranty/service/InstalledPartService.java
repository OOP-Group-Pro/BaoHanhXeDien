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

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InstalledPartService {

    @Autowired
    private InstalledPartRepository installedPartRepository;
    @Autowired
    private VehicleRepository vehicleRepository;

    /**
     * Cập nhật: Thêm @Transactional
     */
    @Transactional // Đảm bảo lưu và gọi trừ kho là một giao dịch
    public InstalledPartResponseDTO installPart(InstalledPartRequestDTO requestDTO) {
        Vehicle vehicle = vehicleRepository.findById(requestDTO.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + requestDTO.getVehicleId()));

        // --- LƯU VÀO DB NỘI BỘ ---
        InstalledPart newPart = new InstalledPart();
        newPart.setPartId(requestDTO.getPartId());
        newPart.setSerialNumber(requestDTO.getSerialNumber());
        newPart.setInstallDate(requestDTO.getInstallDate());
        newPart.setStatus(requestDTO.getStatus());
        newPart.setVehicle(vehicle);

        InstalledPart savedPart = installedPartRepository.save(newPart);

        return convertToDTO(savedPart);
    }
    public InstalledPartResponseDTO getPartById(Long id) {
        InstalledPart part = installedPartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InstalledPart not found with ID: " + id));
        return convertToDTO(part);
    }

    // 3. UPDATE (MỚI)
    @Transactional
    public InstalledPartResponseDTO updatePart(Long id, InstalledPartRequestDTO requestDTO) {
        // Tìm Part cũ
        InstalledPart existingPart = installedPartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InstalledPart not found with ID: " + id));

        // Tìm Vehicle (nếu có đổi xe)
        Vehicle vehicle = vehicleRepository.findById(requestDTO.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + requestDTO.getVehicleId()));

        // Cập nhật các trường
        existingPart.setPartId(requestDTO.getPartId());
        existingPart.setSerialNumber(requestDTO.getSerialNumber());
        existingPart.setInstallDate(requestDTO.getInstallDate());
        existingPart.setStatus(requestDTO.getStatus());
        existingPart.setVehicle(vehicle); // Cho phép gán sang xe khác (nếu cần)

        InstalledPart updatedPart = installedPartRepository.save(existingPart);
        return convertToDTO(updatedPart);
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