package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.response.ApiResponse;
import com.oem.evwarranty.dto.response.VehicleResponseDTO;
import com.oem.evwarranty.entity.Customer;
import com.oem.evwarranty.service.CustomerService;
import com.oem.evwarranty.service.VehicleService;
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

    // POST - Tạo mới
    @PostMapping
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@RequestBody Customer customer) {
        Customer createdCustomer = customerService.createCustomer(customer);
        ApiResponse<Customer> response = ApiResponse.success(
                HttpStatus.CREATED.value(),
                "Customer created successfully.",
                createdCustomer
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // GET - Lấy tất cả
    @GetMapping
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        ApiResponse<List<Customer>> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Successfully retrieved all customers.",
                customers
        );
        return ResponseEntity.ok(response);
    }

    // GET - Lấy theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        ApiResponse<Customer> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Successfully retrieved customer with id " + id,
                customer
        );
        return ResponseEntity.ok(response);
    }

    // PUT - Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(@PathVariable Long id, @RequestBody Customer customerDetails) {
        Customer updatedCustomer = customerService.updateCustomer(id, customerDetails);
        ApiResponse<Customer> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Customer with id " + id + " updated successfully.",
                updatedCustomer
        );
        return ResponseEntity.ok(response);
    }

    // DELETE - Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        ApiResponse<Object> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Customer with id " + id + " deleted successfully."
        );
        return ResponseEntity.ok(response);
    }

    // GET - Lấy tất cả xe của một khách hàng
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
}