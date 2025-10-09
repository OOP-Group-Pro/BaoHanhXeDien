package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.SuccessResponse;
import com.oem.evwarranty.dto.VehicleRequestDTO;
import com.oem.evwarranty.dto.VehicleResponseDTO;
import com.oem.evwarranty.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.projection.EntityProjection.ProjectionType.DTO;


@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    // POST: Tạo một Vehicle mới
    @PostMapping
    public ResponseEntity<SuccessResponse> createVehicle(@RequestBody VehicleRequestDTO vehicleRequest) {
        // 1. Hứng kết quả DTO trả về từ service
        VehicleResponseDTO createdVehicle = vehicleService.createVehicle(vehicleRequest);

        // 2. Tạo response chứa cả thông báo và dữ liệu
        SuccessResponse response = new SuccessResponse(
                HttpStatus.CREATED.value(),
                "Vehicle created successfully.",
                createdVehicle // Dữ liệu của xe vừa tạo
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // GET: Lấy Vehicle theo ID
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    // GET: Lấy Vehicle theo VIN
    @GetMapping("/vin/{vin}")
    public ResponseEntity<VehicleResponseDTO> getVehicleByVin(@PathVariable String vin) {
        return ResponseEntity.ok(vehicleService.getVehicleByVin(vin));
    }

    // PUT: Cập nhật Vehicle
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> updateVehicle(@PathVariable Long id, @RequestBody VehicleRequestDTO vehicleRequest) {
        // 1. Hứng kết quả DTO trả về từ service
        VehicleResponseDTO updatedVehicle = vehicleService.updateVehicle(id, vehicleRequest);

        // 2. Tạo response chứa cả thông báo và dữ liệu đã cập nhật
        SuccessResponse response = new SuccessResponse(
                HttpStatus.OK.value(),
                "Vehicle with id " + id + " updated successfully.",
                updatedVehicle // Dữ liệu của xe đã cập nhật
        );
        return ResponseEntity.ok(response);
    }

    // DELETE: Xóa Vehicle
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        SuccessResponse response = new SuccessResponse(
                HttpStatus.OK.value(),
                "Vehicle with id " + id + " deleted successfully."
        );
        return ResponseEntity.ok(response);
    }
}