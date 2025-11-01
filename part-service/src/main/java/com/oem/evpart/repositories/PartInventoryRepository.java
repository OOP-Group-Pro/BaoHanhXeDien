package com.oem.evpart.repositories;

import com.oem.evpart.models.Part;
import com.oem.evpart.models.PartInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartInventoryRepository extends JpaRepository<PartInventory, Long> {


    Optional<PartInventory> findByPart_PartIdAndLocation(Long partId, String location);


    List<PartInventory> findByPart_PartId(Long partId);


    List<PartInventory> findByLocation(String location);

    //Tìm 1 kho đầu tiên có chứa "part" và số lượng "quantity" lớn hơn hoặc bằng
    Optional<PartInventory> findFirstByPartAndQuantityGreaterThanEqual(Part part, Long quantity);
}