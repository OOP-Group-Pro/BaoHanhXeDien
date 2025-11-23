package com.oem.evcampaign.service;

import com.oem.evcampaign.dto.request.AffectedVehicleCompleteRequest;
import com.oem.evcampaign.dto.request.AffectedVehicleCreateRequest;
import com.oem.evcampaign.dto.request.AffectedVehicleScheduleRequest;
import com.oem.evcampaign.dto.request.AffectedVehicleUpdateRequest;
import com.oem.evcampaign.dto.request.*;
import com.oem.evcampaign.dto.response.AffectedVehicleResponse;
import com.oem.evcampaign.model.enums.AffectedStatus;
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
