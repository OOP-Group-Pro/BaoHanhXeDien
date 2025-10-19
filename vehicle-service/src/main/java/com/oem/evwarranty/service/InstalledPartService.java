package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.request.InstalledPartRequestDTO;
import com.oem.evwarranty.dto.response.InstalledPartResponseDTO;
import com.oem.evwarranty.entity.InstalledPart;
import com.oem.evwarranty.entity.Vehicle;
import com.oem.evwarranty.exception.ResourceNotFoundException;
import com.oem.evwarranty.repository.InstalledPartRepository;
import com.oem.evwarranty.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstalledPartService {

    @Autowired
    private InstalledPartRepository installedPartRepository;
    @Autowired
    private VehicleRepository vehicleRepository; // Dùng để tìm Vehicle

    public InstalledPartResponseDTO installPart(InstalledPartRequestDTO requestDTO) {
        Vehicle vehicle = vehicleRepository.findById(requestDTO.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + requestDTO.getVehicleId()));

        InstalledPart newPart = new InstalledPart();
        newPart.setPartId(requestDTO.getPartId());
        newPart.setSerialNumber(requestDTO.getSerialNumber());
        newPart.setInstallDate(requestDTO.getInstallDate());
        newPart.setStatus(requestDTO.getStatus());
        newPart.setVehicle(vehicle);

        InstalledPart savedPart = installedPartRepository.save(newPart);
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
        // 1. Tìm linh kiện cần xóa
        InstalledPart part = installedPartRepository.findById(installedPartId)
                .orElseThrow(() -> new ResourceNotFoundException("InstalledPart not found with ID: " + installedPartId));

        // 2. Kiểm tra xem nó có thuộc về đúng chiếc xe không
        if (!part.getVehicle().getVehicleId().equals(vehicleId)) {
            throw new ResourceNotFoundException("Part with id " + installedPartId + " does not belong to vehicle with id " + vehicleId);
        }

        // 3. Nếu đúng, thực hiện xóa
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