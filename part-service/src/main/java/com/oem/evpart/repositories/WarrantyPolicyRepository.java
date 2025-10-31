package com.oem.evpart.repositories;


import com.oem.evpart.models.WarrantyPolicy;
import com.oem.evpart.models.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarrantyPolicyRepository extends JpaRepository<WarrantyPolicy, Long> {
    List<WarrantyPolicy> findByPart(Part part);

    List<WarrantyPolicy> findByPart_PartId(Long partId);
}

