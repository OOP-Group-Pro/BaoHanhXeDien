package com.oem.evpart.repositories;


import com.oem.evpart.models.PartAllocation;
import com.oem.evpart.models.PartInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartAllocationRepository extends JpaRepository<PartAllocation, Long> {
    List<PartAllocation> findByInventory(PartInventory inventory);
}
