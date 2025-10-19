package com.oem.evwarranty.repository;

import com.oem.evwarranty.model.WarrantyClaim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim,Long> {
}
