package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.response.ApiResponse;
import com.oem.evwarranty.dto.request.ServiceHistoryRequestDTO;
import com.oem.evwarranty.dto.response.ServiceHistoryResponseDTO;
import com.oem.evwarranty.service.ServiceHistoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/history")
public class ServiceHistoryController {

    @Autowired
    private ServiceHistoryService historyService;

    @PostMapping
    public ResponseEntity<ApiResponse<ServiceHistoryResponseDTO>> addServiceHistory(@Valid @RequestBody ServiceHistoryRequestDTO requestDTO) {
        ServiceHistoryResponseDTO newHistory = historyService.addServiceHistory(requestDTO);
        ApiResponse<ServiceHistoryResponseDTO> response = ApiResponse.success(
                HttpStatus.CREATED.value(), "Service history added successfully.", newHistory);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceHistoryResponseDTO>> getHistoryById(@PathVariable Long id) {
        ServiceHistoryResponseDTO history = historyService.getHistoryById(id);
        ApiResponse<ServiceHistoryResponseDTO> response = ApiResponse.success(
                HttpStatus.OK.value(), "History retrieved successfully.", history);
        return ResponseEntity.ok(response);
    }

    // 3. READ (Get by Technician ID)
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<ApiResponse<List<ServiceHistoryResponseDTO>>> getHistoryByTechnicianId(
            @PathVariable Long technicianId) {

        List<ServiceHistoryResponseDTO> histories = historyService.getHistoryByTechnician(technicianId);
        ApiResponse<List<ServiceHistoryResponseDTO>> response = ApiResponse.success(
                HttpStatus.OK.value(), "History for technician retrieved successfully.", histories);
        return ResponseEntity.ok(response);
    }

    // 4. UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceHistoryResponseDTO>> updateServiceHistory(
            @PathVariable Long id,
            @Valid @RequestBody ServiceHistoryRequestDTO requestDTO) {

        ServiceHistoryResponseDTO updatedHistory = historyService.updateServiceHistory(id, requestDTO);
        ApiResponse<ServiceHistoryResponseDTO> response = ApiResponse.success(
                HttpStatus.OK.value(), "Service history updated successfully.", updatedHistory);
        return ResponseEntity.ok(response);
    }

    // 5. DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteServiceHistory(@PathVariable Long id) {
        historyService.deleteServiceHistory(id);
        ApiResponse<Object> response = ApiResponse.success(
                HttpStatus.OK.value(), "Service history deleted successfully.");
        return ResponseEntity.ok(response);
    }
}