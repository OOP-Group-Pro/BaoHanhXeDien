package com.oem.evwarranty.repository;

import com.oem.evwarranty.dto.ReportDataDto;
import com.oem.evwarranty.model.ClaimPartDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimPartDetailRepository extends JpaRepository<ClaimPartDetail,Long> {
    List<ClaimPartDetail> findAllByClaim_Id(Long claimId);

    //List<ClaimPartDetail> saveAll(List<ClaimPartDetail> claimPartDetails);

    ClaimPartDetail findByClaim_IdAndPartNumber(Long claimId,String partNumber);

    ClaimPartDetail save(ClaimPartDetail claimPartDetail);

    @Query("SELECT new com.oem.evwarranty.dto.ReportDataDto(p.partNumber, SUM(p.quantityRequired)) " +
            "FROM ClaimPartDetail p " +
            "GROUP BY p.partNumber " +
            "ORDER BY SUM(p.quantityRequired) DESC")
    List<ReportDataDto> findTopFaultyParts(Pageable pageable);
}
