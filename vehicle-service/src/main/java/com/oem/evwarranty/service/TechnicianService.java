package com.oem.evwarranty.service;

// Import DTO
import com.oem.evwarranty.dto.response.TechnicianResponseDTO;
import com.oem.evwarranty.entity.Technician;
import com.oem.evwarranty.exception.ResourceNotFoundException;
import com.oem.evwarranty.repository.TechnicianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors; // Thêm import

@Service
public class TechnicianService {

    @Autowired
    private TechnicianRepository technicianRepository;

    // Sửa 1: Trả về DTO
    public TechnicianResponseDTO createTechnician(Technician technician) {
        Technician savedTech = technicianRepository.save(technician);
        return convertToDTO(savedTech);
    }

    // Sửa 2: Trả về List DTO
    public List<TechnicianResponseDTO> getAllTechnicians() {
        return technicianRepository.findAll()
                .stream()
                .map(this::convertToDTO) // Chuyển đổi từng phần tử
                .collect(Collectors.toList());
    }

    // Sửa 3: Trả về DTO
    public TechnicianResponseDTO getTechnicianById(Long id) {
        Technician tech = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + id));
        return convertToDTO(tech);
    }

    public void deleteTechnician(Long id) {
        if (!technicianRepository.existsById(id)) {
            throw new ResourceNotFoundException("Technician not found with ID: " + id);
        }
        technicianRepository.deleteById(id);
    }

    // Sửa 4: Thêm hàm helper để chuyển đổi
    private TechnicianResponseDTO convertToDTO(Technician tech) {
        TechnicianResponseDTO dto = new TechnicianResponseDTO();
        dto.setTechnicianId(tech.getTechnicianId());
        dto.setTechnicianName(tech.getTechnicianName());
        dto.setTechnicianLevel(tech.getTechnicianLevel());
        dto.setStatus(tech.getStatus());
        dto.setPhoneNum(tech.getPhoneNum());
        // Lưu ý: centerId sẽ được Feign Client thêm vào ở tầng cao hơn
        return dto;
    }
}