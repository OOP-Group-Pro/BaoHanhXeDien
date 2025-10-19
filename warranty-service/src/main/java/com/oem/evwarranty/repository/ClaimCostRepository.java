package com.oem.evwarranty.repository;

import com.oem.evwarranty.model.ClaimStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimCostRepository extends JpaRepository<ClaimStatusLog,Long> {
}
