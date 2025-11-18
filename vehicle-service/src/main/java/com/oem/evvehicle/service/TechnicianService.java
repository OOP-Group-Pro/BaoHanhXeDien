// File: .../service/TechnicianService.java
package com.oem.evvehicle.service;

// Import DTO
import com.oem.evvehicle.dto.request.TechnicianRequestDTO; // <-- THÊM DIESER
import com.oem.evvehicle.dto.response.TechnicianResponseDTO;
import com.oem.evvehicle.entity.Technician;
import com.oem.evvehicle.exception.ResourceNotFoundException;
import com.oem.evvehicle.repository.TechnicianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TechnicianService {

    @Autowired
    private TechnicianRepository technicianRepository;

    // SỬA 1: Nhận vào DTO, không nhận Entity
    public TechnicianResponseDTO createTechnician(TechnicianRequestDTO requestDTO) {

        // Chuyển DTO sang Entity để lưu
        Technician newTech = new Technician();
        newTech.setTechnicianName(requestDTO.getTechnicianName());
        newTech.setTechnicianLevel(requestDTO.getTechnicianLevel());
        newTech.setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus() : "Active");
        // (Set các trường khác nếu DTO có, ví dụ: phoneNum)
        // newTech.setPhoneNum(requestDTO.getPhoneNum());

        Technician savedTech = technicianRepository.save(newTech);
        return convertToDTO(savedTech);
    }

    // THÊM MỚI: Phương thức Update
    public TechnicianResponseDTO updateTechnician(Long id, TechnicianRequestDTO requestDTO) {
        // 1. Tìm technician cũ
        Technician existingTech = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + id));

        // 2. Cập nhật các trường từ DTO
        existingTech.setTechnicianName(requestDTO.getTechnicianName());
        existingTech.setTechnicianLevel(requestDTO.getTechnicianLevel());
        existingTech.setStatus(requestDTO.getStatus());
        // (Cập nhật các trường khác nếu DTO có)
        // existingTech.setPhoneNum(requestDTO.getPhoneNum());

        // 3. Lưu lại
        Technician updatedTech = technicianRepository.save(existingTech);

        // 4. Trả về DTO
        return convertToDTO(updatedTech);
    }

    // (Hàm này giữ nguyên - đã tốt)
    public List<TechnicianResponseDTO> getAllTechnicians() {
        return technicianRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // (Hàm này giữ nguyên - đã tốt)
    public TechnicianResponseDTO getTechnicianById(Long id) {
        Technician tech = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + id));
        return convertToDTO(tech);
    }

    // (Hàm này giữ nguyên - đã tốt)
    public void deleteTechnician(Long id) {
        if (!technicianRepository.existsById(id)) {
            throw new ResourceNotFoundException("Technician not found with ID: " + id);
        }
        technicianRepository.deleteById(id);
    }

    // (Hàm này giữ nguyên - đã tốt)
    private TechnicianResponseDTO convertToDTO(Technician tech) {
        TechnicianResponseDTO dto = new TechnicianResponseDTO();
        dto.setTechnicianId(tech.getTechnicianId());
        dto.setTechnicianName(tech.getTechnicianName());
        dto.setTechnicianLevel(tech.getTechnicianLevel());
        dto.setStatus(tech.getStatus());
        dto.setPhoneNum(tech.getPhoneNum());
        return dto;
    }
}