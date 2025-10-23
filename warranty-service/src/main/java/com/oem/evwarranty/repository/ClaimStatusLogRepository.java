package com.oem.evwarranty.repository;

import com.oem.evwarranty.model.ClaimStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimStatusLogRepository extends JpaRepository<ClaimStatusLog,Long> {
    List<ClaimStatusLog> findByClaim_IdOrderByTimestampAsc(Long claimId);
}
