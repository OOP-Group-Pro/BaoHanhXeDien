package com.oem.evpart.repositories;


import com.oem.evpart.models.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Optional;

@Repository
public interface PartRepository extends JpaRepository<Part, Long> {
    boolean existsBySerialNumber(String serialNumber);
    Optional<Part> findBySerialNumber(String serialNumber);

    Optional<Part> findByPartType(String type);
}
