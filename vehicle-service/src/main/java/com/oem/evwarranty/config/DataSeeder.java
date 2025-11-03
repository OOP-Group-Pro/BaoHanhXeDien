package com.oem.evwarranty.config;

import com.oem.evwarranty.entity.Customer;
import com.oem.evwarranty.entity.Technician;
import com.oem.evwarranty.entity.Vehicle;
import com.oem.evwarranty.entity.ServiceHistory;
import com.oem.evwarranty.repository.CustomerRepository;
import com.oem.evwarranty.repository.TechnicianRepository;
import com.oem.evwarranty.repository.VehicleRepository;
import com.oem.evwarranty.repository.ServiceHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * Class này sẽ tự động chạy khi khởi động ứng dụng
 * (CHỈ KHI profile là "dev")
 * để chèn dữ liệu mẫu vào DB.
 */
@Component
@Profile("dev") // <-- QUAN TRỌNG: Chỉ chạy khi profile là "dev"
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private TechnicianRepository technicianRepository;
    @Autowired
    private ServiceHistoryRepository serviceHistoryRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public void run(String... args) throws Exception {

        // Chỉ seed khi DB trống
        if (vehicleRepository.count() == 0 && technicianRepository.count() == 0) {

            System.out.println("--- Bắt đầu Seeding Dữ liệu ---");

            // --- 1. Tạo Technician ---
            Technician tech1 = new Technician();
            tech1.setTechnicianId(101L); // Giả sử ID này khớp với User-service
            tech1.setTechnicianLevel("L5");
            tech1.setStatus("Active");
            tech1.setTechnicianName("Nguyen Van A"); // 👈 BẮT BUỘC: thêm dòng này

            // (Bạn có thể set thêm các trường khác)
            technicianRepository.save(tech1);

            // --- Tạo customer ---
            Customer customer1 = new Customer();
            customer1.setCustomerName("Tran Van B");
            customer1.setPhoneNum("0987654321");
            customer1.setEmail("tranb@example.com");
            customerRepository.save(customer1);


            // --- 2. Tạo Vehicle ---
            Vehicle vehicle1 = new Vehicle();
            vehicle1.setVehicleVin("VIN123456789ABCDE");
            vehicle1.setModel("Model S");
            vehicle1.setStatus("Active");
            vehicle1.setCustomer(customer1);
            // (Bạn có thể set thêm các trường khác)
            vehicleRepository.save(vehicle1);

            // --- 3. Tạo Service History (dùng 2 đối tượng trên) ---
            ServiceHistory history1 = new ServiceHistory();
            history1.setDescription("Kiểm tra định kỳ lần đầu");
            history1.setPerformedDate(LocalDateTime.now());
            history1.setVehicle(vehicle1);       // <-- Gán quan hệ
            history1.setTechnician(tech1); // <-- Gán quan hệ
            serviceHistoryRepository.save(history1);

            System.out.println("--- Seeding Hoàn tất ---");
        }
    }
}