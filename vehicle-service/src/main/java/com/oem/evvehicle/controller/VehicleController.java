package com.oem.evvehicle.controller;

import com.oem.evvehicle.dto.response.ApiResponse;
import com.oem.evvehicle.dto.request.VehicleRequestDTO;
import com.oem.evvehicle.dto.response.InstalledPartResponseDTO;
import com.oem.evvehicle.dto.response.ServiceHistoryResponseDTO;
import com.oem.evvehicle.dto.response.VehicleResponseDTO;
import com.oem.evvehicle.service.InstalledPartService;
import com.oem.evvehicle.service.ServiceHistoryService;
import com.oem.evvehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private InstalledPartService installedPartService;
    @Autowired
    private ServiceHistoryService historyService;

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponseDTO>> createVehicle(@Valid @RequestBody VehicleRequestDTO vehicleRequest) {
        VehicleResponseDTO createdVehicle = vehicleService.createVehicle(vehicleRequest);
        ApiResponse<VehicleResponseDTO> response = ApiResponse.success(
                HttpStatus.CREATED.value(), "Vehicle created successfully.", createdVehicle);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponseDTO>> getVehicleById(@PathVariable Long id) {
        VehicleResponseDTO vehicle = vehicleService.getVehicleById(id);
        ApiResponse<VehicleResponseDTO> response = ApiResponse.success(
                HttpStatus.OK.value(), "Successfully retrieved vehicle with id " + id, vehicle);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponseDTO>> updateVehicle(@PathVariable Long id, @RequestBody VehicleRequestDTO vehicleRequest) {
        VehicleResponseDTO updatedVehicle = vehicleService.updateVehicle(id, vehicleRequest);
        ApiResponse<VehicleResponseDTO> response = ApiResponse.success(
                HttpStatus.OK.value(), "Vehicle updated successfully.", updatedVehicle);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        ApiResponse<Object> response = ApiResponse.success(
                HttpStatus.OK.value(), "Vehicle with id " + id + " deleted successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{vehicleId}/parts")
    public ResponseEntity<ApiResponse<List<InstalledPartResponseDTO>>> getPartsForVehicle(@PathVariable Long vehicleId) {
        List<InstalledPartResponseDTO> parts = installedPartService.getPartsByVehicleId(vehicleId);
        ApiResponse<List<InstalledPartResponseDTO>> response = ApiResponse.success(
                HttpStatus.OK.value(), "Successfully retrieved parts for vehicle " + vehicleId, parts);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/vin/{vin}")
    public ResponseEntity<ApiResponse<VehicleResponseDTO>> getVehicleByVin(@PathVariable String vin) {
        VehicleResponseDTO vehicle = vehicleService.getVehicleByVin(vin); // Bạn cần tạo service method này
        ApiResponse<VehicleResponseDTO> response = ApiResponse.success(
                HttpStatus.OK.value(), "Successfully retrieved vehicle with vin " + vin, vehicle);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{vehicleId}/parts/{installedPartId}")
    public ResponseEntity<ApiResponse<Object>> removePartFromVehicle(@PathVariable Long vehicleId, @PathVariable Long installedPartId) {
        installedPartService.removePartFromVehicle(vehicleId, installedPartId);
        ApiResponse<Object> response = ApiResponse.success(
                HttpStatus.OK.value(), "Part with id " + installedPartId + " removed successfully from vehicle " + vehicleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{vehicleId}/history")
    public ResponseEntity<ApiResponse<List<ServiceHistoryResponseDTO>>> getHistoryForVehicle(@PathVariable Long vehicleId) {
        List<ServiceHistoryResponseDTO> history = historyService.getHistoryByVehicleId(vehicleId);
        ApiResponse<List<ServiceHistoryResponseDTO>> response = ApiResponse.success(
                HttpStatus.OK.value(), "Successfully retrieved history for vehicle " + vehicleId, history);
        return ResponseEntity.ok(response);
    }

    /**
     * API này được warranty-service sử dụng để xác thực nhanh
     * một số VIN có tồn tại trong hệ thống hay không.
     */
    @GetMapping("/validate/{vin}")
    public ResponseEntity<Boolean> validateVin(@PathVariable String vin) {
        boolean exists = vehicleService.isVinExists(vin);
        return ResponseEntity.ok(exists);
    }

    /**
     * API này được warranty-service sử dụng để lấy TÊN khách hàng
     * đang sở hữu xe, dựa trên số VIN.
     */
    @GetMapping("/{vin}/customer-name")
    public ResponseEntity<String> getCustomerNameByVin(@PathVariable("vin") String vin) {
        String customerName = vehicleService.getCustomerNameByVin(vin);
        return ResponseEntity.ok(customerName);
    }

    @PostMapping("/vins/customer-names")
    public ResponseEntity<Map<String, String>> getCustomerNamesByVins(@RequestBody List<String> vins) {
        Map<String, String> nameMap = vehicleService.getCustomerNamesByVins(vins);
        return ResponseEntity.ok(nameMap);
    }

    /**
     * API Tìm kiếm xe (Search Vehicles)
     * URL: GET /api/v1/vehicles?keyword=VF8&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<VehicleResponseDTO>>> searchVehicles(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "vehicleId", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<VehicleResponseDTO> result = vehicleService.searchVehicles(keyword, pageable);

        ApiResponse<Page<VehicleResponseDTO>> response = ApiResponse.success(
                HttpStatus.OK.value(), "Search vehicles successfully.", result);
        return ResponseEntity.ok(response);
    }

}