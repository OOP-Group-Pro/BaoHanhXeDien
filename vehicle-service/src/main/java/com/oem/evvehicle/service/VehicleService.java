package com.oem.evvehicle.service;

import com.oem.evvehicle.dto.event.VehicleCreatedEvent;
import com.oem.evvehicle.dto.response.CustomerResponseDTO;
import com.oem.evvehicle.dto.request.VehicleRequestDTO;
import com.oem.evvehicle.dto.response.VehicleResponseDTO;
import com.oem.evvehicle.entity.Customer;
import com.oem.evvehicle.entity.Vehicle;
import com.oem.evvehicle.exception.ResourceNotFoundException;
import com.oem.evvehicle.rabbitmq.VehicleProducer;
import com.oem.evvehicle.repository.CustomerRepository;
import com.oem.evvehicle.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private VehicleProducer vehicleProducer;
    // CREATE: Tạo một Vehicle mới
    @Transactional // [NÊN THÊM] Để đảm bảo lưu DB thành công rồi mới gửi tin
    public VehicleResponseDTO createVehicle(VehicleRequestDTO vehicleRequest) {
        // ... (Giữ nguyên logic kiểm tra VIN và tìm Customer cũ của bạn) ...
        if (vehicleRepository.findByVehicleVin(vehicleRequest.getVehicleVin()).isPresent()) {
            throw new DataIntegrityViolationException("VIN '" + vehicleRequest.getVehicleVin() + "' already exists.");
        }
        Customer owner = customerRepository.findById(vehicleRequest.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + vehicleRequest.getCustomerId()));

        // ... (Giữ nguyên logic lưu Vehicle) ...
        Vehicle newVehicle = new Vehicle();
        newVehicle.setVehicleVin(vehicleRequest.getVehicleVin());
        newVehicle.setLicensePlate(vehicleRequest.getLicensePlate());
        newVehicle.setModel(vehicleRequest.getModel());
        newVehicle.setCustomer(owner);
        // Giả sử manufacturedDate là ngày mua/kích hoạt
        newVehicle.setManufacturedDate(java.time.LocalDateTime.now());

        Vehicle savedVehicle = vehicleRepository.save(newVehicle);

        // 2. [THÊM] Gửi Event sang RabbitMQ (Side Effect)
        try {
            VehicleCreatedEvent event = new VehicleCreatedEvent(
                    savedVehicle.getVehicleId(),
                    savedVehicle.getVehicleVin(),
                    savedVehicle.getLicensePlate(),
                    owner.getCustomerId(), // Hoặc owner.getUserId() tùy logic bên Warranty cần gì
                    savedVehicle.getManufacturedDate().toString() // Chuyển date sang string
            );

            vehicleProducer.sendVehicleCreatedEvent(event);

        } catch (Exception e) {
            // Log lỗi RabbitMQ nhưng KHÔNG rollback transaction DB (xe vẫn được tạo)
            System.err.println("⚠️ Error sending RabbitMQ event: " + e.getMessage());
        }

        return convertToDTO(savedVehicle);
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

    // UPDATE: Cập nhật thông tin Vehicle
    @Caching(evict = {
            @CacheEvict(value = "vehicles_id", key = "#id"),
            @CacheEvict(value = "vehicles_vin", allEntries = true), // Vì ko biết VIN cũ, xóa hết hoặc query để lấy VIN
            @CacheEvict(value = "customer_vehicles", allEntries = true)
    })
    public VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO vehicleRequest) {
        Vehicle vehicleToUpdate = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        Customer owner = customerRepository.findById(vehicleRequest.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer (owner) not found with ID: " + vehicleRequest.getCustomerId()));

        vehicleToUpdate.setModel(vehicleRequest.getModel());
        vehicleToUpdate.setLicensePlate(vehicleRequest.getLicensePlate());
        vehicleToUpdate.setCustomer(owner); // (Giờ đã đúng kiểu)

        Vehicle updatedVehicle = vehicleRepository.save(vehicleToUpdate);
        return convertToDTO(updatedVehicle);
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