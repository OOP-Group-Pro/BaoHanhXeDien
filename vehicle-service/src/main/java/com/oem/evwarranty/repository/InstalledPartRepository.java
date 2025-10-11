package com.oem.evwarranty.repository;

import com.oem.evwarranty.entity.InstalledPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstalledPartRepository extends JpaRepository<InstalledPart, Long> {
    // Tìm tất cả các linh kiện đã lắp đặt cho một xe cụ thể
    List<InstalledPart> findByVehicleVehicleId(Long vehicleId);
}