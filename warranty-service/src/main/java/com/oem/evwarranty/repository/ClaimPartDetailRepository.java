package com.oem.evwarranty.repository;

import com.oem.evwarranty.model.ClaimPartDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimPartDetailRepository extends JpaRepository<ClaimPartDetail,Long> {
    List<ClaimPartDetail> findAllByClaim_Id(Long claimId);

    //List<ClaimPartDetail> saveAll(List<ClaimPartDetail> claimPartDetails);

    ClaimPartDetail findByClaim_IdAndPartNumber(Long claimId,String partNumber);

    ClaimPartDetail save(ClaimPartDetail claimPartDetail);
}
