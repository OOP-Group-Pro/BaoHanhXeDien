package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.request.InstalledPartRequestDTO;
import com.oem.evwarranty.dto.response.InstalledPartResponseDTO;
import com.oem.evwarranty.dto.response.ApiResponse;
import com.oem.evwarranty.service.InstalledPartService;
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
    public ResponseEntity<ApiResponse<InstalledPartResponseDTO>> installPart(@RequestBody InstalledPartRequestDTO requestDTO) {
        InstalledPartResponseDTO installedPart = installedPartService.installPart(requestDTO);
        ApiResponse<InstalledPartResponseDTO> response = ApiResponse.success(
                HttpStatus.CREATED.value(), "Part installed successfully.", installedPart);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{vehicleId}/parts/{installedPartId}")
    public ResponseEntity<ApiResponse<Object>> removePartFromVehicle(
            @PathVariable Long vehicleId,
            @PathVariable Long installedPartId) {

        installedPartService.removePartFromVehicle(vehicleId, installedPartId);

        ApiResponse<Object> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Part with id " + installedPartId + " removed successfully from vehicle with id: " + vehicleId
        );

        return ResponseEntity.ok(response);
    }
}