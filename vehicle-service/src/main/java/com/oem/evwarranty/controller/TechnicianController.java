package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<Technician>> createTechnician(@RequestBody Technician technician) {
        Technician createdTech = technicianService.createTechnician(technician);
        ApiResponse<Technician> response = ApiResponse.success(
                HttpStatus.CREATED.value(), "Technician created successfully.", createdTech);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Technician>>> getAllTechnicians() {
        List<Technician> technicians = technicianService.getAllTechnicians();
        ApiResponse<List<Technician>> response = ApiResponse.success(
                HttpStatus.OK.value(), "Successfully retrieved all technicians.", technicians);
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