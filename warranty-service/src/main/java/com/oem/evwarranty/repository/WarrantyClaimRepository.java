package com.oem.evwarranty.repository;

import com.oem.evwarranty.enums.ClaimStatus;
import com.oem.evwarranty.model.WarrantyClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim,Long> {
    List<WarrantyClaim> getByCurrentStatus(ClaimStatus currentStatus);
}
