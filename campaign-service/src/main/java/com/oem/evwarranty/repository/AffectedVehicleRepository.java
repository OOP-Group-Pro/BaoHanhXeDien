package com.oem.evwarranty.repository;

import com.oem.evwarranty.model.AffectedVehicle;
import com.oem.evwarranty.model.enums.AffectedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AffectedVehicleRepository extends JpaRepository<AffectedVehicle, Long> {

    // kiểm tra VIN đã nằm trong campaign chưa
    boolean existsByCampaignIdAndVehicleVin(Long campaignId, String vehicleVin);

    // search
    Page<AffectedVehicle> findByCampaignId(Long campaignId, Pageable pageable);
    Page<AffectedVehicle> findByCampaignIdAndStatus(Long campaignId, AffectedStatus status, Pageable pageable);
    Page<AffectedVehicle> findByCampaignIdAndVehicleVinContainingIgnoreCase(Long campaignId, String vin, Pageable pageable);

    // lấy 1 bản ghi theo campaign + id (đảm bảo không lạc campaign)
    Optional<AffectedVehicle> findByIdAndCampaignId(Long id, Long campaignId);
}
