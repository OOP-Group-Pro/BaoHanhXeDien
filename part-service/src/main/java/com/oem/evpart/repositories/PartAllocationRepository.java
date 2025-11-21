package com.oem.evpart.repositories;

import com.oem.evpart.models.PartAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartAllocationRepository extends JpaRepository<PartAllocation, Long> {

    /**
     * Tìm tất cả các lần phân bổ phụ tùng cho một Trung tâm dịch vụ cụ thể.
     * @param serviceCenterId ID của Trung tâm dịch vụ.
     * @return Danh sách các lần phân bổ.
     */
    List<PartAllocation> findByServiceCenterId(Integer serviceCenterId);

    /**
     * Tìm tất cả các lần phân bổ xuất phát từ một nguồn kho cụ thể.
     * @param inventoryId ID của bản ghi tồn kho.
     * @return Danh sách các lần phân bổ.
     */
    List<PartAllocation> findByInventory_InventoryId(Long inventoryId);
    List<PartAllocation> findByClaimCode(String claimCode);

}