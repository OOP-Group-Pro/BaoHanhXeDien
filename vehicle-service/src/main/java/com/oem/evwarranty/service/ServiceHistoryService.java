package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.request.ServiceHistoryRequestDTO;
import com.oem.evwarranty.dto.response.ServiceHistoryResponseDTO;
import com.oem.evwarranty.dto.response.TechnicianResponseDTO;
import com.oem.evwarranty.entity.InstalledPart;
import com.oem.evwarranty.entity.ServiceHistory;
import com.oem.evwarranty.entity.Technician;
import com.oem.evwarranty.entity.Vehicle;
import com.oem.evwarranty.exception.ResourceNotFoundException;
import com.oem.evwarranty.repository.InstalledPartRepository;
import com.oem.evwarranty.repository.ServiceHistoryRepository;
import com.oem.evwarranty.repository.TechnicianRepository;
import com.oem.evwarranty.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceHistoryService {

    @Autowired
    private ServiceHistoryRepository historyRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private TechnicianRepository technicianRepository;
    @Autowired
    private InstalledPartRepository installedPartRepository;

    public ServiceHistoryResponseDTO addServiceHistory(ServiceHistoryRequestDTO requestDTO) {
        Vehicle vehicle = vehicleRepository.findById(requestDTO.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + requestDTO.getVehicleId()));

        Technician technician = technicianRepository.findById(requestDTO.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + requestDTO.getTechnicianId()));

        ServiceHistory newHistory = new ServiceHistory();
        newHistory.setDescription(requestDTO.getDescription());
        newHistory.setPerformedDate(requestDTO.getPerformedDate());
        newHistory.setVehicle(vehicle);
        newHistory.setTechnician(technician);

        if (requestDTO.getPartIds() != null && !requestDTO.getPartIds().isEmpty()) {
            List<InstalledPart> parts = installedPartRepository.findAllById(requestDTO.getPartIds());
            newHistory.setPartsInvolved(new HashSet<>(parts));
        }

        ServiceHistory savedHistory = historyRepository.save(newHistory);
        return convertToDTO(savedHistory);
    }

    public List<ServiceHistoryResponseDTO> getHistoryByVehicleId(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
        }
        return historyRepository.findByVehicleVehicleId(vehicleId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ServiceHistoryResponseDTO convertToDTO(ServiceHistory history) {
        Technician technician = history.getTechnician();
        TechnicianResponseDTO techDTO = new TechnicianResponseDTO();
        techDTO.setTechnicianId(technician.getTechnicianId());
        techDTO.setTechnicianName(technician.getTechnicianName());
        techDTO.setTechnicianLevel(technician.getTechnicianLevel());

        ServiceHistoryResponseDTO historyDTO = new ServiceHistoryResponseDTO();
        historyDTO.setServiceHistoryId(history.getServiceHistoryId());
        historyDTO.setDescription(history.getDescription());
        historyDTO.setPerformedDate(history.getPerformedDate());
        historyDTO.setVehicleId(history.getVehicle().getVehicleId());
        historyDTO.setTechnician(techDTO);
        return historyDTO;
    }
}