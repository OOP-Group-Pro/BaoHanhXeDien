package com.oem.evpart.repositories;


import com.oem.evpart.models.Part;
import com.oem.evpart.models.PartInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartInventoryRepository extends JpaRepository<PartInventory, Long> {
    List<PartInventory> findByPart(Part part);
    Optional<PartInventory> findByPartAndLocation(Part part, String location);
    boolean existsByPartAndLocation(Part part, String location);
}
