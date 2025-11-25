package com.oem.evpart.repositories;


import com.oem.evpart.models.WarrantyPolicy;
import com.oem.evpart.models.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarrantyPolicyRepository extends JpaRepository<WarrantyPolicy, Long> {

    List<WarrantyPolicy> findByParts(List<Part> parts);
    Optional<WarrantyPolicy> findByParts_PartId(Long partId);
}

