package com.oem.evcampaign.repository;

import com.oem.evcampaign.model.Campaign;
import com.oem.evcampaign.model.enums.CampaignStatus;
import com.oem.evcampaign.model.enums.CampaignType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    // Check code trùng
    boolean existsByCode(String code);

    // --- SỬA ĐOẠN NÀY ---
    // Lấy 1 campaign theo code (nếu bạn có dùng chỗ khác)
    Optional<Campaign> findByCode(String code);

    // Query thông minh:
    // 1. keyword: Tìm gần đúng trong cả CODE và TITLE (không phân biệt hoa thường)
    // 2. status: Nếu null thì lấy hết, nếu có giá trị thì lọc
    // 3. type: Tương tự status
    @Query("SELECT c FROM Campaign c WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR c.status = :status) " +
            "AND (:type IS NULL OR c.type = :type)")
    Page<Campaign> search(@Param("keyword") String keyword,
                          @Param("status") CampaignStatus status,
                          @Param("type") CampaignType type,
                          Pageable pageable);
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