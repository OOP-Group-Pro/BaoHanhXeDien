package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.request.TechnicianRequestDTO;
import com.oem.evwarranty.dto.response.ApiResponse;
import com.oem.evwarranty.dto.response.TechnicianResponseDTO;
import com.oem.evwarranty.service.TechnicianService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/technicians")
public class TechnicianController {

    @Autowired
    private TechnicianService technicianService;

    @PostMapping
    public ResponseEntity<ApiResponse<TechnicianResponseDTO>> createTechnician(
            @Valid @RequestBody TechnicianRequestDTO techRequest) { // <-- ĐÃ SỬA

        // 1. Service nhận RequestDTO và trả về ResponseDTO
        TechnicianResponseDTO createdTech = technicianService.createTechnician(techRequest); // <-- ĐÃ SỬA

        // 2. ApiResponse cũng phải chứa DTO
        ApiResponse<TechnicianResponseDTO> response = ApiResponse.success(
                HttpStatus.CREATED.value(), "Technician created successfully.", createdTech);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TechnicianResponseDTO>>> getAllTechnicians() {
        List<TechnicianResponseDTO> technicians = technicianService.getAllTechnicians();
        ApiResponse<List<TechnicianResponseDTO>> response = ApiResponse.success(
                HttpStatus.OK.value(), "Successfully retrieved all technicians.", technicians);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicianResponseDTO>> getTechnicianById(@PathVariable Long id) {
        TechnicianResponseDTO technician = technicianService.getTechnicianById(id);
        ApiResponse<TechnicianResponseDTO> response = ApiResponse.success(
                HttpStatus.OK.value(), "Successfully retrieved technician.", technician);
        return ResponseEntity.ok(response);
    }

    // (Hàm này đã đúng)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicianResponseDTO>> updateTechnician(
            @PathVariable Long id,
            @Valid @RequestBody TechnicianRequestDTO techRequest) {

        TechnicianResponseDTO updatedTech = technicianService.updateTechnician(id, techRequest);
        ApiResponse<TechnicianResponseDTO> response = ApiResponse.success(
                HttpStatus.OK.value(), "Technician updated successfully.", updatedTech);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteTechnician(@PathVariable Long id) {
        technicianService.deleteTechnician(id);
        ApiResponse<Object> response = ApiResponse.success(
                HttpStatus.OK.value(), "Technician with id " + id + " deleted successfully.");
        return ResponseEntity.ok(response);
    }
}