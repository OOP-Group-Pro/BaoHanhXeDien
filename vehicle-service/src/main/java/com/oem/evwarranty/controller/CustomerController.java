package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.SuccessResponse; // Import lớp mới
import com.oem.evwarranty.dto.VehicleResponseDTO;
import com.oem.evwarranty.entity.Customer;
import com.oem.evwarranty.entity.Vehicle;
import com.oem.evwarranty.service.CustomerService;
import com.oem.evwarranty.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;
    @Autowired // Thêm VehicleService vào
    private VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> createCustomer(@RequestBody Customer customer) {
        // 1. Hứng kết quả trả về từ service
        Customer createdCustomer = customerService.createCustomer(customer);

        // 2. Tạo response, truyền cả 'createdCustomer' vào
        SuccessResponse response = new SuccessResponse(
                HttpStatus.CREATED.value(), // 201
                "Customer created successfully.",
                createdCustomer // Dữ liệu của khách hàng vừa tạo
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> updateCustomer(@PathVariable Long id, @RequestBody Customer customerDetails) {
        customerService.updateCustomer(id, customerDetails);
        SuccessResponse response = new SuccessResponse(
                HttpStatus.OK.value(), // 200
                "Customer with id " + id + " updated successfully."
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        SuccessResponse response = new SuccessResponse(
                HttpStatus.OK.value(), // 200
                "Customer with id " + id + " deleted successfully."
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Customer> getCustomerByEmail(@RequestParam String email) {
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }
    @GetMapping("/{customerId}/vehicles")
    public ResponseEntity<List<VehicleResponseDTO>> getVehiclesForCustomer(@PathVariable Long customerId) {
        List<VehicleResponseDTO> vehicles = vehicleService.getVehiclesByCustomerId(customerId);
        return ResponseEntity.ok(vehicles);
    }
}