package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.response.ApiResponse;
import com.oem.evwarranty.dto.request.ServiceHistoryRequestDTO;
import com.oem.evwarranty.dto.response.ServiceHistoryResponseDTO;
import com.oem.evwarranty.service.ServiceHistoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}