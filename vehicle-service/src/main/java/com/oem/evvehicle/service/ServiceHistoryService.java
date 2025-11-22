package com.oem.evvehicle.service;

import com.oem.evvehicle.client.vehicle.CenterClient;
import com.oem.evvehicle.client.vehicle.TechnicianClient;
import com.oem.evvehicle.dto.external.CenterDetailsDTO;
import com.oem.evvehicle.dto.external.TechnicianDetailsDTO;
import com.oem.evvehicle.dto.request.ServiceHistoryRequestDTO;
import com.oem.evvehicle.dto.response.ServiceHistoryResponseDTO;
import com.oem.evvehicle.entity.InstalledPart;
import com.oem.evvehicle.entity.ServiceHistory;
import com.oem.evvehicle.entity.Technician;
import com.oem.evvehicle.entity.Vehicle;
import com.oem.evvehicle.exception.ResourceNotFoundException;
import com.oem.evvehicle.repository.InstalledPartRepository;
import com.oem.evvehicle.repository.ServiceHistoryRepository;
import com.oem.evvehicle.repository.TechnicianRepository;
import com.oem.evvehicle.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ServiceHistoryService {

    // (Các @Autowired Repository giữ nguyên)
    @Autowired
    private ServiceHistoryRepository historyRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private TechnicianRepository technicianRepository;
    @Autowired
    private InstalledPartRepository installedPartRepository;

    @Autowired
    private TechnicianClient technicianClient;
    @Autowired
    private CenterClient centerClient;

    /**
     * Hàm này không thay đổi
     */
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
    public ServiceHistoryResponseDTO getHistoryById(Long id) {
        ServiceHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceHistory not found with ID: " + id));
        return convertToDTO(history);
    }

    /**
     * 3. READ (Get by Vehicle ID - Bạn đã có)
     * (Hàm này được VehicleController gọi)
     */
    public List<ServiceHistoryResponseDTO> getHistoryByVehicleId(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
        }
        return historyRepository.findByVehicleVehicleId(vehicleId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 4. READ (Get by Technician ID - MỚI)
     */
    public List<ServiceHistoryResponseDTO> getHistoryByTechnician(Long technicianId) {
        List<ServiceHistory> histories = historyRepository.findByTechnicianTechnicianId(technicianId);
        return histories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 5. UPDATE (MỚI)
     */
    public ServiceHistoryResponseDTO updateServiceHistory(Long id, ServiceHistoryRequestDTO requestDTO) {
        // Tìm bản ghi cũ
        ServiceHistory existingHistory = historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceHistory not found with ID: " + id));

        // Tìm các Entity liên quan (có thể người dùng muốn đổi KTV)
        Vehicle vehicle = vehicleRepository.findById(requestDTO.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + requestDTO.getVehicleId()));

        Technician technician = technicianRepository.findById(requestDTO.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + requestDTO.getTechnicianId()));

        // Cập nhật các trường
        existingHistory.setDescription(requestDTO.getDescription());
        existingHistory.setPerformedDate(requestDTO.getPerformedDate());
        existingHistory.setVehicle(vehicle);
        existingHistory.setTechnician(technician);

        // Cập nhật PartIds
        if (requestDTO.getPartIds() != null) {
            if (requestDTO.getPartIds().isEmpty()) {
                existingHistory.getPartsInvolved().clear();
            } else {
                List<InstalledPart> parts = installedPartRepository.findAllById(requestDTO.getPartIds());
                existingHistory.setPartsInvolved(new HashSet<>(parts));
            }
        }

        ServiceHistory updatedHistory = historyRepository.save(existingHistory);
        return convertToDTO(updatedHistory);
    }

    /**
     * 6. DELETE (MỚI)
     */
    public void deleteServiceHistory(Long id) {
        if (!historyRepository.existsById(id)) {
            throw new ResourceNotFoundException("ServiceHistory not found with ID: " + id);
        }
        historyRepository.deleteById(id);
    }

    // --- HÀM CONVERT DTO (ĐÃ ĐƯỢC CẬP NHẬT) ---
    /**
     * Chuyển đổi ServiceHistory Entity (nội bộ) sang DTO "phẳng" (đã làm giàu).
     * Phiên bản này giả định serviceCenterId trả về là Long.
     */
    private ServiceHistoryResponseDTO convertToDTO(ServiceHistory history) {

        String technicianName = "Không rõ KTV";
        String centerName = "Không rõ trung tâm";
        Long technicianId = history.getTechnician().getTechnicianId();

        try {
            // --- BƯỚC 1: GỌI CLIENT 1 (USERCLIENT) ---
            TechnicianDetailsDTO techDetails = technicianClient.getTechnicianDetails(technicianId);

            technicianName = techDetails.getFullName();
            Long centerIdLong = techDetails.getServiceCenterId(); // <-- ĐÃ LÀ LONG

            if (centerIdLong != null) {
                try {
                    // --- BƯỚC 2: GỌI CLIENT 2 (CENTERCLIENT) ---
                    // (Không cần chuyển đổi, gọi trực tiếp)
                    CenterDetailsDTO centerDetails = centerClient.getCenterDetails(centerIdLong);
                    centerName = centerDetails.getCenterName();

                } catch (Exception e) {
                    log.error("Lỗi khi gọi CenterClient với ID {}: {}", centerIdLong, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi gọi UserClient với ID {}: {}", technicianId, e.getMessage());
        }

        // --- BƯỚC 3: TỔNG HỢP VÀO DTO "PHẲNG" ---
        ServiceHistoryResponseDTO dto = new ServiceHistoryResponseDTO();
        dto.setServiceHistoryId(history.getServiceHistoryId());
        dto.setDescription(history.getDescription());
        dto.setPerformedDate(history.getPerformedDate());
        dto.setTechnicianName(technicianName);
        dto.setCenterName(centerName);

        return dto;
    }
}