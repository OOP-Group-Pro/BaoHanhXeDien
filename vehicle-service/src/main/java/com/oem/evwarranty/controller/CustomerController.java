package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.request.CustomerRequestDTO;
import com.oem.evwarranty.dto.response.ApiResponse;
import com.oem.evwarranty.dto.response.CustomerResponseDTO;
import com.oem.evwarranty.dto.response.VehicleResponseDTO;
import com.oem.evwarranty.service.CustomerService;
import com.oem.evwarranty.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private VehicleService vehicleService;

    // POST - Sửa: Nhận RequestDTO, trả về ResponseDTO
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> createCustomer(
            @Valid @RequestBody CustomerRequestDTO requestDTO) {

        CustomerResponseDTO createdCustomer = customerService.createCustomer(requestDTO);
        ApiResponse<CustomerResponseDTO> response = ApiResponse.success(
                HttpStatus.CREATED.value(),
                "Customer created successfully.",
                createdCustomer
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // GET
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponseDTO>>> getAllCustomers() {
        List<CustomerResponseDTO> customers = customerService.getAllCustomers();
        ApiResponse<List<CustomerResponseDTO>> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Successfully retrieved all customers.",
                customers
        );
        return ResponseEntity.ok(response);
    }

    // GET
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> getCustomerById(@PathVariable Long id) {
        CustomerResponseDTO customer = customerService.getCustomerById(id);
        ApiResponse<CustomerResponseDTO> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Successfully retrieved customer with id " + id,
                customer
        );
        return ResponseEntity.ok(response);
    }

    // PUT
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDTO requestDTO) { // Thêm @Valid

        CustomerResponseDTO updatedCustomer = customerService.updateCustomer(id, requestDTO);
        ApiResponse<CustomerResponseDTO> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Customer with id " + id + " updated successfully.",
                updatedCustomer
        );
        return ResponseEntity.ok(response);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        ApiResponse<Object> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Customer with id " + id + " deleted successfully."
        );
        return ResponseEntity.ok(response);
    }

    // GET (Nested)
    @GetMapping("/{customerId}/vehicles")
    public ResponseEntity<ApiResponse<List<VehicleResponseDTO>>> getVehiclesForCustomer(@PathVariable Long customerId) {
        List<VehicleResponseDTO> vehicles = vehicleService.getVehiclesByCustomerId(customerId);
        ApiResponse<List<VehicleResponseDTO>> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Successfully retrieved vehicles for customer " + customerId,
                vehicles
        );
        return ResponseEntity.ok(response);
    }

    // GET (Search)
    @GetMapping("/search/email") // Thêm /search/email để rõ ràng hơn
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> getCustomerByEmail(
            @RequestParam String email) {

        CustomerResponseDTO customer = customerService.getCustomerByEmail(email);
        ApiResponse<CustomerResponseDTO> response = ApiResponse.success(
                HttpStatus.OK.value(), "Customer retrieved successfully.", customer);
        return ResponseEntity.ok(response);
    }
}