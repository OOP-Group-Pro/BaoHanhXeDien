package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.request.*;
import com.oem.evwarranty.dto.response.AffectedVehicleResponse;
import com.oem.evwarranty.model.enums.AffectedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AffectedVehicleService {

    AffectedVehicleResponse create(AffectedVehicleCreateRequest req);

    AffectedVehicleResponse update(Integer affectedId, AffectedVehicleUpdateRequest req);

    void delete(Integer affectedId);

    AffectedVehicleResponse get(Integer affectedId);

    Page<AffectedVehicleResponse> search(
            Integer campaignId, String vinKeyword, AffectedStatus status, Pageable pageable);

    AffectedVehicleResponse markNotified(Integer affectedId);

    AffectedVehicleResponse schedule(Integer affectedId, AffectedVehicleScheduleRequest req);

    AffectedVehicleResponse markCompleted(Integer affectedId, AffectedVehicleCompleteRequest req);
}
