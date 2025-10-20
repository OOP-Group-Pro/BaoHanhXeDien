// File: src/main/java/com/oem/evpart/repositories/InstalledPartRepository.java
package com.oem.evpart.repositories;

import com.oem.evpart.models.InstalledPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstalledPartRepository extends JpaRepository<InstalledPart, Long> {

    // The status parameter should be the Enum type, not String
    Optional<InstalledPart> findByVehicleVinAndPart_PartIdAndStatus(String vehicleVin, Long partId, InstalledPart.Status status);

    // FIX: Add the missing method declaration
    List<InstalledPart> findByVehicleVinOrderByInstallDateDesc(String vin);
}