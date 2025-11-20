package com.oem.evvehicle.config;

import com.oem.evvehicle.entity.Customer;
import com.oem.evvehicle.repository.CustomerRepository;
import com.oem.evvehicle.entity.Technician;
import com.oem.evvehicle.entity.Vehicle;
import com.oem.evvehicle.entity.ServiceHistory;
import com.oem.evvehicle.repository.TechnicianRepository;
import com.oem.evvehicle.repository.VehicleRepository;
import com.oem.evvehicle.repository.ServiceHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private TechnicianRepository technicianRepository;
    @Autowired
    private ServiceHistoryRepository serviceHistoryRepository;

    @Override
    public void run(String... args) throws Exception {

        // Sửa: Thêm customerRepository.count() vào
        if (vehicleRepository.count() == 0 && technicianRepository.count() == 0 && customerRepository.count() == 0) {

            System.out.println("--- Bắt đầu Seeding Dữ liệu ---");

            // --- 1. TẠO CUSTOMER TRƯỚC ---
            Customer customer1 = new Customer();
            customer1.setCustomerName("Khach Hang Mau");
            customer1.setEmail("customer@example.com");
            customer1.setPhoneNum("0123456789");
            customerRepository.save(customer1); // <-- Lưu Customer

            // --- 2. Tạo Technician ---
            Technician tech1 = new Technician();
            // (Bỏ dòng setTechnicianId nếu bạn dùng Auto Increment)
            tech1.setTechnicianName("Nguyen Van A"); // (Giữ lại tên đúng)
            tech1.setTechnicianLevel("L5");
            tech1.setStatus("Active");
            technicianRepository.save(tech1);


            // --- 3. Tạo Vehicle ---
            Vehicle vehicle1 = new Vehicle();
            vehicle1.setVehicleVin("VIN123456789ABCDE");
            vehicle1.setModel("Model S");
            vehicle1.setStatus("Active");
            vehicle1.setCustomer(customer1);
            vehicle1.setCurrentOdometer(500000L);
            vehicle1.setWarrantyStartDate(LocalDate.of(2017, 07, 17));

            // --- THÊM BIỂN SỐ XE ---
            vehicle1.setLicensePlate("59-G1 12345");

            vehicleRepository.save(vehicle1);

            // --- 4. Tạo Service History ---
            ServiceHistory history1 = new ServiceHistory();
            history1.setDescription("Kiểm tra định kỳ lần đầu");
            history1.setPerformedDate(LocalDateTime.now());
            history1.setVehicle(vehicle1);
            history1.setTechnician(tech1);
            serviceHistoryRepository.save(history1);

            System.out.println("--- Seeding Hoàn tất ---");
        }
    }
}