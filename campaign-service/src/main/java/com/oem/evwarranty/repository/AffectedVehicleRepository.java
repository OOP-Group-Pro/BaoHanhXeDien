package com.oem.evwarranty.repository;

import com.oem.evwarranty.model.AffectedVehicle;
import com.oem.evwarranty.model.enums.AffectedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffectedVehicleRepository extends JpaRepository<AffectedVehicle, Integer> {

    boolean existsByCampaignIdAndVehicleVin(Integer campaignId, String vehicleVin);

    Page<AffectedVehicle> findByCampaignIdAndVehicleVinContainingIgnoreCase(
            Integer campaignId, String vinKeyword, Pageable pageable);

    Page<AffectedVehicle> findByCampaignIdAndStatus(
            Integer campaignId, AffectedStatus status, Pageable pageable);
}
