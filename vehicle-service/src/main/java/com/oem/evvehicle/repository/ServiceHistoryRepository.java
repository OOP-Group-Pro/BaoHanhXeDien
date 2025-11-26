package com.oem.evvehicle.repository;

import com.oem.evvehicle.entity.ServiceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceHistoryRepository extends JpaRepository<ServiceHistory, Long> {
    // Find all history records for a specific vehicle
    List<ServiceHistory> findByVehicleVehicleId(Long vehicleId);
    List<ServiceHistory> findByTechnicianTechnicianId(Long technicianId);
    List<ServiceHistory> findByVehicle_VehicleVinOrderByPerformedDateDesc(String vehicleVin);
}