package com.oem.evpart.repositories;

import com.oem.evpart.models.Part;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartRepository extends JpaRepository<Part, Long> {
    boolean existsBySerialNumber(String serialNumber);

    Optional<Part> findByPartId(Long partId);

    List<Part> findByPartIdIn(List<Long> partIds);

    Optional<Part> findBySerialNumber(String serialNumber);

    List<Part> findAllBySerialNumberIn(List<String> serialNumbers);

    Optional<Part> findByPartType(String type);

    List<Part> findAllByPartType(String partType);

    List<Part> findAllByPartTypeIn(List<String> partNumbers);

    @Query("SELECT DISTINCT p FROM Part p LEFT JOIN FETCH p.inventories WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.serialNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:partType IS NULL OR :partType = '' OR p.partType = :partType)")
    Page<Part> searchParts(@Param("keyword") String keyword,
                           @Param("partType") String partType,
                           Pageable pageable);
}