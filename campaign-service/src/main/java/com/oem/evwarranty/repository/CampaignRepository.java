package com.oem.evwarranty.repository;

import com.oem.evwarranty.model.Campaign;
import com.oem.evwarranty.model.enums.CampaignStatus;
import com.oem.evwarranty.model.enums.CampaignType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Integer> {

    // Check code trùng
    boolean existsByCode(String code);

    // Lấy 1 campaign theo code (nếu bạn có dùng chỗ khác)
    Optional<Campaign> findByCode(String code);

    // Tìm kiếm theo code (contains, ignore case) + phân trang
    Page<Campaign> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    // Tìm kiếm theo code + status + type (đúng như ServiceImpl đang gọi)
    Page<Campaign> findByCodeContainingIgnoreCaseAndStatusAndType(
            String code,
            CampaignStatus status,
            CampaignType type,
            Pageable pageable
    );
}