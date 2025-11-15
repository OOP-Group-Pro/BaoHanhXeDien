package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.request.InstalledPartRequestDTO;
import com.oem.evwarranty.dto.response.InstalledPartResponseDTO;
import com.oem.evwarranty.dto.response.ApiResponse;
import com.oem.evwarranty.service.InstalledPartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parts")
public class InstalledPartController {

    @Autowired
    private InstalledPartService installedPartService;

    @PostMapping("/install")
    public ResponseEntity<ApiResponse<InstalledPartResponseDTO>> installPart(@Valid @RequestBody InstalledPartRequestDTO requestDTO) {
        InstalledPartResponseDTO installedPart = installedPartService.installPart(requestDTO);
        ApiResponse<InstalledPartResponseDTO> response = ApiResponse.success(
                HttpStatus.CREATED.value(), "Part installed successfully.", installedPart);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    // 2. READ (Get By ID - MỚI)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InstalledPartResponseDTO>> getInstalledPartById(
            @PathVariable Long id) {

        InstalledPartResponseDTO part = installedPartService.getPartById(id);
        ApiResponse<InstalledPartResponseDTO> response = ApiResponse.success(
                HttpStatus.OK.value(), "Part retrieved successfully.", part);
        return ResponseEntity.ok(response);
    }

    // 3. UPDATE (MỚI)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InstalledPartResponseDTO>> updateInstalledPart(
            @PathVariable Long id,
            @Valid @RequestBody InstalledPartRequestDTO requestDTO) {

        InstalledPartResponseDTO updatedPart = installedPartService.updatePart(id, requestDTO);
        ApiResponse<InstalledPartResponseDTO> response = ApiResponse.success(
                HttpStatus.OK.value(), "Part updated successfully.", updatedPart);
        return ResponseEntity.ok(response);
    }
}