package com.oem.evpart.repositories;


import com.oem.evpart.models.Part;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    List<Part> findAllByPartTypeIn(List<String> partNumbers);

    // Tìm theo Tên HOẶC Mã (SerialNumber), không phân biệt hoa thường
    Page<Part> findByNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCase(String name,
                                                                                String serialNumber,
                                                                                Pageable pageable);
}
