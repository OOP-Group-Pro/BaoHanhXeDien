package com.oem.evpart.repositories;


import com.oem.evpart.models.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartRepository extends JpaRepository<Part, Long> {
    boolean existsBySerialNumber(String serialNumber);
    List<Part> findAllByPartTypeIn(List<String> partNumbers);
}
