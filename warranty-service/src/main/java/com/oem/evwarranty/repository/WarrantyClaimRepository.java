package com.oem.evwarranty.repository;

import com.oem.evwarranty.enums.ClaimStatus;
import com.oem.evwarranty.model.WarrantyClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim,Long>, JpaSpecificationExecutor<WarrantyClaim> {
    List<WarrantyClaim> getByCurrentStatus(ClaimStatus currentStatus);

    Page findAll (Specification spec, Pageable pageable);

    Optional<WarrantyClaim> findByClaimCode(String claimCode);
}
