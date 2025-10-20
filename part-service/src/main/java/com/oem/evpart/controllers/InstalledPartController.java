package com.oem.evpart.controllers;

import com.oem.evpart.dto.request.InstalledPartRequest;
import com.oem.evpart.dto.request.UpdateInstallationStatusRequest; // Bạn cần tạo DTO này
import com.oem.evpart.dto.response.InstalledPartResponse;
import com.oem.evpart.services.InstalledPartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/installed-parts")
@RequiredArgsConstructor
public class InstalledPartController {

    private final InstalledPartService installedPartService;

    @PostMapping("/record")
    public ResponseEntity<InstalledPartResponse> recordInstallation(@Valid @RequestBody InstalledPartRequest request) {
        InstalledPartResponse response = installedPartService.recordPartInstallation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/history/{vin}")
    public ResponseEntity<List<InstalledPartResponse>> getInstallationHistory(@PathVariable String vin) {
        return ResponseEntity.ok(installedPartService.getHistoryByVehicleVin(vin));
    }

    @PatchMapping("/{installedId}/status")
    public ResponseEntity<InstalledPartResponse> updateStatus(@PathVariable Long installedId, @Valid @RequestBody UpdateInstallationStatusRequest request) {
        return ResponseEntity.ok(installedPartService.updateInstallationStatus(installedId, request.getStatus()));
    }
}

