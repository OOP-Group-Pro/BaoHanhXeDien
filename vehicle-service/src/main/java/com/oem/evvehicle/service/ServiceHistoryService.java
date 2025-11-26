package com.oem.evvehicle.service;

import com.oem.evvehicle.client.vehicle.CenterClient;
import com.oem.evvehicle.client.vehicle.PartServiceClient;
import com.oem.evvehicle.client.vehicle.TechnicianClient;
import com.oem.evvehicle.dto.external.CenterDetailsDTO;
import com.oem.evvehicle.dto.external.TechnicianDetailsDTO;
import com.oem.evvehicle.dto.request.DecrementStockRequest;
import com.oem.evvehicle.dto.request.ServiceHistoryRequestDTO;
import com.oem.evvehicle.dto.request.SyncServiceHistoryRequest;
import com.oem.evvehicle.dto.response.InstalledPartResponseDTO;
import com.oem.evvehicle.dto.response.ServiceHistoryResponseDTO;
import com.oem.evvehicle.entity.InstalledPart;
import com.oem.evvehicle.entity.ServiceHistory;
import com.oem.evvehicle.entity.Technician;
import com.oem.evvehicle.entity.Vehicle;
import com.oem.evvehicle.entity.enums.InstallStatus;
import com.oem.evvehicle.exception.BusinessException;
import com.oem.evvehicle.exception.ResourceNotFoundException;
import com.oem.evvehicle.repository.InstalledPartRepository;
import com.oem.evvehicle.repository.ServiceHistoryRepository;
import com.oem.evvehicle.repository.TechnicianRepository;
import com.oem.evvehicle.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ServiceHistoryService {

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

    @Autowired
    private PartServiceClient partServiceClient; // ✅ Dùng PartServiceClient

    /**
     * Hàm này không thay đổi
     */
    // 1. HÀM ADD SERVICE HISTORY (LOGIC MỚI)
    @Transactional(rollbackFor = Exception.class)
    public ServiceHistoryResponseDTO addServiceHistory(ServiceHistoryRequestDTO requestDTO) {
        // A. Validate
        Vehicle vehicle = vehicleRepository.findById(requestDTO.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        Technician technician = technicianRepository.findById(requestDTO.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

        // B. Tạo History
        ServiceHistory newHistory = new ServiceHistory();
        newHistory.setDescription(requestDTO.getDescription());
        newHistory.setPerformedDate(requestDTO.getPerformedDate());
        newHistory.setOdometerReading(requestDTO.getOdometerReading());
        newHistory.setVehicle(vehicle);
        newHistory.setTechnician(technician);

        // Lưu trước để có ID (nếu cần dùng cho logic phức tạp sau này)
        newHistory = historyRepository.save(newHistory);

        // C. Xử lý Phụ tùng & Trừ kho
        if (requestDTO.getPartsToInstall() != null && !requestDTO.getPartsToInstall().isEmpty()) {
            Set<InstalledPart> installedParts = new HashSet<>();

            for (ServiceHistoryRequestDTO.PartInstallationInfo info : requestDTO.getPartsToInstall()) {
                // C.1: Lưu vào bảng installed_parts
                InstalledPart part = new InstalledPart();
                part.setVehicle(vehicle);
                part.setPartId(info.getPartId());
                part.setSerialNumber(info.getSerialNumber());
                part.setInstallDate(requestDTO.getPerformedDate());
                part.setStatus(info.getStatus());

                // Lưu InstalledPart
                part = installedPartRepository.save(part);
                installedParts.add(part);

                // C.2: Gọi TRỪ KHO (Part-Service)
                try {
                    // Lấy thông tin Service Center từ Technician (để biết trừ kho nào)
                    // Lưu ý: Cần đảm bảo Technician có serviceCenterId hợp lệ
                    TechnicianDetailsDTO techDetails = technicianClient.getTechnicianDetails(technician.getTechnicianId());
                    String locationCode = String.valueOf(techDetails.getServiceCenterId());

                    partServiceClient.decrementStock(new DecrementStockRequest(
                            info.getPartId(),
                            1, // Mỗi serial là 1 cái
                            locationCode
                    ));
                } catch (Exception e) {
                    log.error("Lỗi trừ kho: {}", e.getMessage());
                    throw new BusinessException("Không thể trừ kho: " + e.getMessage());
                }
            }

            // C.3: Link vào History
            newHistory.setPartsInvolved(installedParts);
            historyRepository.save(newHistory);
        }

        // D. Cập nhật ODO xe
        if (vehicle.getCurrentOdometer() == null ||
                requestDTO.getOdometerReading() > vehicle.getCurrentOdometer()) {
            vehicle.setCurrentOdometer(requestDTO.getOdometerReading());
            vehicleRepository.save(vehicle);
        }

        return convertToDTO(newHistory);
    }

    // 5. UPDATE (ĐÃ SỬA ĐỂ KHỚP DTO MỚI)
    @Transactional
    public ServiceHistoryResponseDTO updateServiceHistory(Long id, ServiceHistoryRequestDTO requestDTO) {
        ServiceHistory existingHistory = historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceHistory not found with ID: " + id));

        Vehicle vehicle = vehicleRepository.findById(requestDTO.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        Technician technician = technicianRepository.findById(requestDTO.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

        existingHistory.setDescription(requestDTO.getDescription());
        existingHistory.setPerformedDate(requestDTO.getPerformedDate());
        existingHistory.setOdometerReading(requestDTO.getOdometerReading());
        existingHistory.setVehicle(vehicle);
        existingHistory.setTechnician(technician);

        // Logic Update Parts: Tạm thời Clear cũ, Add mới (Cẩn thận: sẽ tạo ra bản ghi InstalledPart mới)
        // Để đơn giản cho đồ án, ta làm lại set mới từ đầu.
        if (requestDTO.getPartsToInstall() != null) {
            // Xóa liên kết cũ
            existingHistory.getPartsInvolved().clear();

            if (!requestDTO.getPartsToInstall().isEmpty()) {
                Set<InstalledPart> newParts = new HashSet<>();
                for (ServiceHistoryRequestDTO.PartInstallationInfo info : requestDTO.getPartsToInstall()) {
                    InstalledPart part = new InstalledPart();
                    part.setVehicle(vehicle);
                    part.setPartId(info.getPartId());
                    part.setSerialNumber(info.getSerialNumber());
                    part.setInstallDate(requestDTO.getPerformedDate());
                    part.setStatus(info.getStatus());
                    newParts.add(installedPartRepository.save(part));
                }
                existingHistory.setPartsInvolved(newParts);
            }
        }

        ServiceHistory updatedHistory = historyRepository.save(existingHistory);
        return convertToDTO(updatedHistory);
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

    public ServiceHistoryResponseDTO getHistoryById(Long id) {
        // 1. Tìm trong DB
        ServiceHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceHistory not found with ID: " + id));

        // 2. Convert sang DTO (Hàm convertToDTO đã viết ở dưới cùng file)
        return convertToDTO(history);
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
        // --- UPDATE MỚI: Map các trường vừa thêm ---
        // Lấy VIN từ đối tượng Vehicle liên kết (Sửa từ Long thành String)
        dto.setVehicleVin(history.getVehicle().getVehicleVin());

        // Lấy ODO
        dto.setOdometerReading(history.getOdometerReading());
        // -------------------------------------------
        // BỔ SUNG: Map danh sách phụ tùng liên quan
        if (history.getPartsInvolved() != null && !history.getPartsInvolved().isEmpty()) {
            List<InstalledPartResponseDTO> partDTOs = history.getPartsInvolved().stream()
                    .map(part -> {
                        InstalledPartResponseDTO pDto = new InstalledPartResponseDTO();
                        pDto.setInstalledId(part.getInstalledId());
                        pDto.setSerialNumber(part.getSerialNumber());
                        pDto.setPartId(part.getPartId());
                        pDto.setStatus(part.getStatus());
                        // pDto.setPartName(...) // Nếu có thể lấy tên part
                        return pDto;
                    })
                    .collect(Collectors.toList());
            dto.setParts(partDTOs);
        }

        return dto;
    }

    ///////////////////////////////////////

    @Transactional(rollbackFor = Exception.class)
    public ServiceHistoryResponseDTO createServiceHistory(ServiceHistoryRequestDTO request) {

        // 1. Tìm Vehicle & Technician (Validate)
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Xe không tồn tại"));

        // 🔴 SỬA LẠI ĐOẠN TÌM TECHNICIAN
        Technician technician = technicianRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Kỹ thuật viên không tồn tại"));

        // 2. Tạo Service History
        ServiceHistory history = new ServiceHistory();
        history.setVehicle(vehicle);
        history.setPerformedDate(request.getPerformedDate());
        history.setDescription(request.getDescription());
        history.setOdometerReading(request.getOdometerReading());

        // ✅ Gán biến technician đã tìm được vào (Lỗi sẽ biến mất)
        history.setTechnician(technician);

        // Lưu trước để có ID
        history = historyRepository.save(history);

        // 3. Xử lý Phụ tùng (Nếu có)
        if (request.getPartsToInstall() != null && !request.getPartsToInstall().isEmpty()) {
            Set<InstalledPart> installedParts = new HashSet<>();

            for (ServiceHistoryRequestDTO.PartInstallationInfo info : request.getPartsToInstall()) {
                // 3a. Tạo InstalledPart
                InstalledPart part = new InstalledPart();
                part.setVehicle(vehicle);
                part.setPartId(info.getPartId());
                part.setSerialNumber(info.getSerialNumber());
                part.setInstallDate(request.getPerformedDate());
                part.setStatus(info.getStatus());

                // Lưu InstalledPart
                part = installedPartRepository.save(part);
                installedParts.add(part);

                // 3b. GỌI TRỪ KHO (QUAN TRỌNG)
                // Gọi sang Part-Service để giảm số lượng tồn kho
                try {
                    partServiceClient.decrementStock(new DecrementStockRequest(
                            info.getPartId(),
                            1, // Số lượng luôn là 1 cho mỗi serial
                            "SERVICE_CENTER_XXX" // Lấy ID trạm từ User context
                    ));
                } catch (Exception e) {
                    throw new BusinessException("Lỗi trừ kho: " + e.getMessage());
                }
            }

            // 3c. Liên kết với History
            history.setPartsInvolved(installedParts);
            historyRepository.save(history);
        }

        // 4. Cập nhật ODO hiện tại của xe (Nếu ODO mới lớn hơn cũ)
        if (request.getOdometerReading() > vehicle.getCurrentOdometer()) {
            vehicle.setCurrentOdometer(request.getOdometerReading());
            vehicleRepository.save(vehicle);
        }

        return convertToDTO(history);
    }

    @Transactional
    public void syncHistoryFromWarranty(SyncServiceHistoryRequest request) {
        // 1. Tìm Xe theo VIN
        // Giả định bạn có hàm findByVehicleVin trong Repository, nếu chưa có dùng findById
        Vehicle vehicle = vehicleRepository.findByVehicleVin(request.getVin())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với VIN: " + request.getVin()));

        // 2. Tìm Kỹ thuật viên (Để gán vào lịch sử)
        // Lưu ý: ID này đến từ User Service, cần đảm bảo bảng Technician đã đồng bộ hoặc có dữ liệu
        Technician technician = technicianRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Kỹ thuật viên không tồn tại ID: " + request.getTechnicianId()));

        // 3. Tạo Service History
        ServiceHistory history = new ServiceHistory();
        history.setVehicle(vehicle);
        history.setTechnician(technician);
        history.setPerformedDate(request.getPerformedDate());
        history.setDescription(request.getDescription());
        history.setOdometerReading(request.getOdometerReading());

        // Lưu history trước để có ID
        history = historyRepository.save(history);

        // 4. Lưu Phụ tùng lắp đặt (Installed Parts)
        if (request.getReplacedParts() != null && !request.getReplacedParts().isEmpty()) {
            Set<InstalledPart> installedParts = new HashSet<>();

            for (SyncServiceHistoryRequest.SyncPartItem item : request.getReplacedParts()) {
                InstalledPart part = new InstalledPart();
                part.setVehicle(vehicle);
                part.setSerialNumber(item.getSerialNumber()); // Serial mới
                part.setInstallDate(request.getPerformedDate());
                part.setStatus(InstallStatus.INSTALLED); // Đánh dấu là Đã lắp

                // ⚠️ Lưu ý: Entity InstalledPart yêu cầu partId (Long).
                // Warranty chỉ gửi partNumber (String).
                // Tạm thời set 0 hoặc fake ID nếu chưa có bảng mapping.
                part.setPartId(0L);

                // Lưu vào DB
                part = installedPartRepository.save(part);
                installedParts.add(part);
            }

            // Liên kết phụ tùng với lịch sử
            history.setPartsInvolved(installedParts);
            historyRepository.save(history);
        }

        // 5. Cập nhật ODO hiện tại của xe (Nếu ODO mới lớn hơn cũ)
        if (vehicle.getCurrentOdometer() == null || request.getOdometerReading() > vehicle.getCurrentOdometer()) {
            vehicle.setCurrentOdometer(request.getOdometerReading());
            vehicleRepository.save(vehicle);
        }
    }
}

