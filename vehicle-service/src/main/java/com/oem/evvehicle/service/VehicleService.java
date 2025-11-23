package com.oem.evvehicle.service;

import com.oem.evvehicle.dto.response.CustomerResponseDTO;
import com.oem.evvehicle.dto.request.VehicleRequestDTO;
import com.oem.evvehicle.dto.response.VehicleResponseDTO;
import com.oem.evvehicle.entity.Customer;
import com.oem.evvehicle.entity.Vehicle;
import com.oem.evvehicle.exception.ResourceNotFoundException;
import com.oem.evvehicle.repository.CustomerRepository;
import com.oem.evvehicle.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // CREATE: Tạo một Vehicle mới
    public VehicleResponseDTO createVehicle(VehicleRequestDTO vehicleRequest) {
        // Kiểm tra xem VIN đã tồn tại chưa
        if (vehicleRepository.findByVehicleVin(vehicleRequest.getVehicleVin()).isPresent()) {
            throw new DataIntegrityViolationException("VIN '" + vehicleRequest.getVehicleVin() + "' already exists.");
        }

        Customer owner = customerRepository.findById(vehicleRequest.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Vehicle newVehicle = new Vehicle();
        newVehicle.setVehicleVin(vehicleRequest.getVehicleVin());
        newVehicle.setLicensePlate(vehicleRequest.getLicensePlate());
        newVehicle.setModel(vehicleRequest.getModel());
        newVehicle.setCustomer(owner);

        // --- UPDATE MỚI CHO LOGIC BẢO HÀNH ---
        // Mặc định nếu không nhập ngày bán thì lấy ngày hiện tại
        newVehicle.setWarrantyStartDate(
                vehicleRequest.getWarrantyStartDate() != null ? vehicleRequest.getWarrantyStartDate() : LocalDate.now()
        );
        // Mặc định ODO ban đầu là 0 nếu không nhập
        newVehicle.setCurrentOdometer(
                vehicleRequest.getCurrentOdometer() != null ? vehicleRequest.getCurrentOdometer() : 0L
        );
        // -------------------------------------

        Vehicle savedVehicle = vehicleRepository.save(newVehicle);
        return convertToDTO(savedVehicle);
    }

    // 2. HÀM UPDATE
    @Caching(evict = {
            @CacheEvict(value = "vehicles_id", key = "#id"),
            @CacheEvict(value = "vehicles_vin", allEntries = true), // Vì ko biết VIN cũ, xóa hết hoặc query để lấy VIN
            @CacheEvict(value = "customer_vehicles", allEntries = true)
    })
    public VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO vehicleRequest) {
        Vehicle vehicleToUpdate = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        Customer owner = customerRepository.findById(vehicleRequest.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        vehicleToUpdate.setModel(vehicleRequest.getModel());
        vehicleToUpdate.setLicensePlate(vehicleRequest.getLicensePlate());
        vehicleToUpdate.setCustomer(owner);

        // --- UPDATE MỚI ---
        // Cho phép sửa ngày bảo hành nếu nhập sai (Admin operation)
        if (vehicleRequest.getWarrantyStartDate() != null) {
            vehicleToUpdate.setWarrantyStartDate(vehicleRequest.getWarrantyStartDate());
        }
        // Cho phép cập nhật ODO thủ công (tuy nhiên thường sẽ update qua Service History)
        if (vehicleRequest.getCurrentOdometer() != null) {
            vehicleToUpdate.setCurrentOdometer(vehicleRequest.getCurrentOdometer());
        }
        // ------------------

        Vehicle updatedVehicle = vehicleRepository.save(vehicleToUpdate);
        return convertToDTO(updatedVehicle);
    }

    // READ: Lấy Vehicle theo ID
    @Cacheable(value = "vehicles_id", key = "#id")
    public VehicleResponseDTO getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with Customer ID: " + id));
        return convertToDTO(vehicle);
    }

    // READ: Lấy Vehicle theo VIN
    @Cacheable(value = "vehicles_vin", key = "#vin")
    public VehicleResponseDTO getVehicleByVin(String vin) {
        Vehicle vehicle = vehicleRepository.findByVehicleVin(vin)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with VIN: " + vin));
        return convertToDTO(vehicle);
    }

    // READ: Lấy tất cả Vehicle của một Customer
    @Cacheable(value = "customer_vehicles", key = "#customerId")
    public List<VehicleResponseDTO> getVehiclesByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        }

        List<Vehicle> vehicles = vehicleRepository.findByCustomerCustomerId(customerId);
        return vehicles.stream().map(this::convertToDTO).collect(Collectors.toList());
    }


    // DELETE: Xóa một Vehicle
    @Caching(evict = {
            @CacheEvict(value = "vehicles_id", key = "#id"),
            @CacheEvict(value = "vehicles_vin", allEntries = true),
            @CacheEvict(value = "customer_vehicles", allEntries = true)
    })
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
        vehicleDTO.setLicensePlate(vehicle.getLicensePlate());
        vehicleDTO.setManufacturedDate(vehicle.getManufacturedDate());
        vehicleDTO.setStatus(vehicle.getStatus());
        vehicleDTO.setCustomer(customerDTO);
        // --- UPDATE MỚI: Mapping thêm dữ liệu quan trọng ---
        vehicleDTO.setWarrantyStartDate(vehicle.getWarrantyStartDate());
        vehicleDTO.setCurrentOdometer(vehicle.getCurrentOdometer());
        // --------------------------------------------------

        return vehicleDTO;
    }

    public boolean isVinExists(String vin) {
        return vehicleRepository.existsByVehicleVin(vin); // Giả sử bạn có phương thức này
    }

    @Cacheable(value = "customer_name_by_vin", key = "#vin")
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

    public Map<String, String> getCustomerNamesByVins (List<String> vins) {
        List<Vehicle> vehicles = vehicleRepository.findAllByVehicleVinIn(vins);

        Map<String, String> customerNames = new HashMap<>();
        vehicles.forEach(vehicle -> {
            customerNames.put(vehicle.getVehicleVin(), vehicle.getCustomer().getCustomerName());
        });
        return customerNames;
    }

}