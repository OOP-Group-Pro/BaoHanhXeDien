package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.response.CustomerResponseDTO;
import com.oem.evwarranty.dto.request.VehicleRequestDTO;
import com.oem.evwarranty.dto.response.VehicleResponseDTO;
import com.oem.evwarranty.entity.Customer;
import com.oem.evwarranty.entity.Vehicle;
import com.oem.evwarranty.exception.ResourceNotFoundException;
import com.oem.evwarranty.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CustomerService customerService;

    // CREATE: Tạo một Vehicle mới
    public VehicleResponseDTO createVehicle(VehicleRequestDTO vehicleRequest) {
        // Kiểm tra xem VIN đã tồn tại chưa
        if (vehicleRepository.findByVehicleVin(vehicleRequest.getVehicleVin()).isPresent()) {
            throw new DataIntegrityViolationException("VIN '" + vehicleRequest.getVehicleVin() + "' already exists.");
        }

        Customer owner = customerService.getCustomerById(vehicleRequest.getCustomerId());
        Vehicle newVehicle = new Vehicle();
        newVehicle.setVehicleVin(vehicleRequest.getVehicleVin());
        newVehicle.setModel(vehicleRequest.getModel());
        newVehicle.setCustomer(owner);

        Vehicle savedVehicle = vehicleRepository.save(newVehicle);
        return convertToDTO(savedVehicle);
    }

    // READ: Lấy Vehicle theo ID
    public VehicleResponseDTO getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with Customer ID: " + id));
        return convertToDTO(vehicle);
    }

    // READ: Lấy Vehicle theo VIN
    public VehicleResponseDTO getVehicleByVin(String vin) {
        Vehicle vehicle = vehicleRepository.findByVehicleVin(vin)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with VIN: " + vin));
        return convertToDTO(vehicle);
    }

    // READ: Lấy tất cả Vehicle của một Customer
    public List<VehicleResponseDTO> getVehiclesByCustomerId(Long customerId) {
        customerService.getCustomerById(customerId); // Kiểm tra customer tồn tại
        List<Vehicle> vehicles = vehicleRepository.findByCustomerCustomerId(customerId);
        return vehicles.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // UPDATE: Cập nhật thông tin Vehicle
    public VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO vehicleRequest) {
        Vehicle vehicleToUpdate = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        Customer owner = customerService.getCustomerById(vehicleRequest.getCustomerId());

        vehicleToUpdate.setModel(vehicleRequest.getModel());
        vehicleToUpdate.setCustomer(owner);
        // Lưu ý: Không nên cho phép cập nhật VIN

        Vehicle updatedVehicle = vehicleRepository.save(vehicleToUpdate);
        return convertToDTO(updatedVehicle);
    }

    // DELETE: Xóa một Vehicle
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + id);
        }
        vehicleRepository.deleteById(id);
    }

    // Phương thức private để chuyển đổi Entity sang DTO
    private VehicleResponseDTO convertToDTO(Vehicle vehicle) {
        CustomerResponseDTO customerDTO = new CustomerResponseDTO();
        Customer customer = vehicle.getCustomer();
        customerDTO.setCustomerId(customer.getCustomerId());
        customerDTO.setCustomerName(customer.getCustomerName());
        customerDTO.setEmail(customer.getEmail());

        VehicleResponseDTO vehicleDTO = new VehicleResponseDTO();
        vehicleDTO.setVehicleId(vehicle.getVehicleId());
        vehicleDTO.setVehicleVin(vehicle.getVehicleVin());
        vehicleDTO.setModel(vehicle.getModel());
        vehicleDTO.setManufacturedDate(vehicle.getManufacturedDate());
        vehicleDTO.setStatus(vehicle.getStatus());
        vehicleDTO.setCustomer(customerDTO);

        return vehicleDTO;
    }

    public boolean isVinExists(String vin) {
        return vehicleRepository.existsByVin(vin); // Giả sử bạn có phương thức này
    }
    public String getCustomerNameByVin(String vin) {
        // 1. Tìm xe bằng VIN
        Vehicle vehicle = vehicleRepository.findByVehicleVin(vin)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with VIN: " + vin));

        // 2. Lấy đối tượng Customer liên kết
        Customer customer = vehicle.getCustomer();

        // 3. Kiểm tra xem có khách hàng không
        if (customer == null) {
            throw new ResourceNotFoundException("Vehicle " + vin + " is not associated with any customer.");
        }

        // 4. Trả về tên khách hàng
        return customer.getCustomerName();
    }

}