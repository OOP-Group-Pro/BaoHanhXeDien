package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.response.ApiResponse;
import com.oem.evwarranty.dto.response.TechnicianResponseDTO;
import com.oem.evwarranty.entity.Technician;
import com.oem.evwarranty.service.TechnicianService;
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
    public ResponseEntity<ApiResponse<TechnicianResponseDTO>> createTechnician(@RequestBody Technician technician) {
        // 1. Service trả về DTO
        TechnicianResponseDTO createdTech = technicianService.createTechnician(technician);

        // 2. ApiResponse cũng phải chứa DTO
        ApiResponse<TechnicianResponseDTO> response = ApiResponse.success(
                HttpStatus.CREATED.value(), "Technician created successfully.", createdTech);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TechnicianResponseDTO>>> getAllTechnicians() {
        // 1. Service trả về List<DTO>
        List<TechnicianResponseDTO> technicians = technicianService.getAllTechnicians();

        // 2. ApiResponse cũng phải chứa List<DTO>
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
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteTechnician(@PathVariable Long id) {
        technicianService.deleteTechnician(id);
        ApiResponse<Object> response = ApiResponse.success(
                HttpStatus.OK.value(), "Technician with id " + id + " deleted successfully.");
        return ResponseEntity.ok(response);
    }
}