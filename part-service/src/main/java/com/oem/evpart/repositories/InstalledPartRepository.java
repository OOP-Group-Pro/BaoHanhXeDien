package com.oem.evpart.repositories;



import com.oem.evpart.models.InstalledPart;
import com.oem.evpart.models.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstalledPartRepository extends JpaRepository<InstalledPart, Long> {
    List<InstalledPart> findByVehicleVin(String vehicleVin);
    List<InstalledPart> findByPart(Part part);
}

