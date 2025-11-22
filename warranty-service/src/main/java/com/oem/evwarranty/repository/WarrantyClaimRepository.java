package com.oem.evwarranty.repository;

import com.oem.evwarranty.dto.ReportDataDto;
import com.oem.evwarranty.enums.ClaimStatus;
import com.oem.evwarranty.model.WarrantyClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim,Long>, JpaSpecificationExecutor<WarrantyClaim> {
    List<WarrantyClaim> getByCurrentStatus(ClaimStatus currentStatus);

    Page findAll (Specification spec, Pageable pageable);

    Optional<WarrantyClaim> findByClaimCode(String claimCode);

    // 1. Thống kê theo Trạng thái
    // Mẹo: Dùng CONCAT('', enum) để chuyển Enum thành String an toàn nhất trong JPQL
    @Query("SELECT c.currentStatus, COUNT(c) FROM WarrantyClaim c GROUP BY c.currentStatus")
    List<Object[]> countClaimsByStatusRaw();

    // Trả về danh sách Object[]: [Month(Integer), Count(Long)]
    @Query("SELECT FUNCTION('MONTH', c.dateCreated), COUNT(c) " +
            "FROM WarrantyClaim c " +
            "WHERE FUNCTION('YEAR', c.dateCreated) = :year " +
            "GROUP BY FUNCTION('MONTH', c.dateCreated) " +
            "ORDER BY FUNCTION('MONTH', c.dateCreated)")
    List<Object[]> countClaimsByMonthRaw(@Param("year") int year);
}
