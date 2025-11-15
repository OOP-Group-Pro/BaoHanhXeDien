package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.request.*;
import com.oem.evwarranty.dto.response.AffectedVehicleResponse;
import com.oem.evwarranty.model.enums.AffectedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AffectedVehicleService {

    AffectedVehicleResponse create(AffectedVehicleCreateRequest req);

    AffectedVehicleResponse update(Long affectedId, AffectedVehicleUpdateRequest req);

    void delete(Long affectedId);              // giữ để tương thích cũ (nếu còn dùng)
    AffectedVehicleResponse get(Long affectedId); // giữ để tương thích cũ

    // ===== mới: ràng buộc theo campaign =====
    AffectedVehicleResponse getByCampaign(Long campaignId, Long affectedId);
    void deleteByCampaign(Long campaignId, Long affectedId);

    Page<AffectedVehicleResponse> search(Long campaignId, String vinKeyword,
                                         AffectedStatus status, Pageable pageable);

    AffectedVehicleResponse markNotified(Long affectedId);
    AffectedVehicleResponse schedule(Long affectedId, AffectedVehicleScheduleRequest req);
    AffectedVehicleResponse markCompleted(Long affectedId, AffectedVehicleCompleteRequest req);
}
