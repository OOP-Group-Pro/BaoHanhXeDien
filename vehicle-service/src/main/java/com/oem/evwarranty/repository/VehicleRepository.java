package com.oem.evwarranty.repository;

import com.oem.evwarranty.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
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
}