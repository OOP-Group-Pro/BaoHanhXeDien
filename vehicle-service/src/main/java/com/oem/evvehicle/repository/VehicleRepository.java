package com.oem.evvehicle.repository;

import com.oem.evvehicle.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // Spring sẽ tự tạo query để tìm tất cả Vehicle theo customerId
    List<Vehicle> findByCustomerCustomerId(Long customerId);
    Optional<Vehicle> findByVehicleVin(String vehicleVin);
    List<Vehicle> findAllByVehicleVinIn(List<String> vins);

    boolean existsByVehicleVin(String vehicleVin);

    // ⬇️ THÊM MỚI: Tìm kiếm xe theo từ khóa (VIN hoặc Model hoặc Biển số) với Phân trang
    @Query("SELECT v FROM Vehicle v WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(v.vehicleVin) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.model) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Vehicle> searchVehicles(@Param("keyword") String keyword, Pageable pageable);
}