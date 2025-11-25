package com.oem.evcampaign.controller;

import com.oem.evcampaign.config.ApiConstants;
import com.oem.evcampaign.dto.request.*;
import com.oem.evcampaign.dto.response.AffectedVehicleResponse;
import com.oem.evcampaign.dto.response.PageCacheDto;
import com.oem.evcampaign.model.enums.AffectedStatus;
import com.oem.evcampaign.service.AffectedVehicleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/campaigns/{campaignId}/affected-vehicles")
public class AffectedVehicleController {

    private final AffectedVehicleService service;

    public AffectedVehicleController(AffectedVehicleService service) {
        this.service = service;
    }

    @PostMapping
    public AffectedVehicleResponse create(@PathVariable Long campaignId,
                                          @Valid @RequestBody AffectedVehicleCreateRequest req) {
        req.setCampaignId(campaignId);
        return service.create(req);
    }

    // --- ĐOẠN ĐÃ SỬA ---
    @GetMapping
    public ResponseEntity<PageCacheDto<AffectedVehicleResponse>> search(
            @PathVariable Long campaignId,
            @RequestParam(required = false) String vin,
            @RequestParam(required = false) AffectedStatus status,
            Pageable pageable) {

        // Service đã trả về PageCacheDto, ta chỉ cần bọc vào ResponseEntity.ok
        return ResponseEntity.ok(service.search(campaignId, vin, status, pageable));
    }
    // -------------------

    @GetMapping("/{affectedId}")
    public AffectedVehicleResponse get(@PathVariable Long campaignId,
                                       @PathVariable Long affectedId) {
        return service.getByCampaign(campaignId, affectedId);
    }

    @DeleteMapping("/{affectedId}")
    public ResponseEntity<Void> delete(@PathVariable Long campaignId,
                                       @PathVariable Long affectedId) {
        service.deleteByCampaign(campaignId, affectedId);
        return ResponseEntity.noContent().build();
    }

    // SCHEDULE
    @PostMapping("/{affectedId}/schedule")
    public AffectedVehicleResponse schedule(@PathVariable Long campaignId,
                                            @PathVariable Long affectedId,
                                            @Valid @RequestBody AffectedVehicleScheduleRequest req) {
        return service.schedule(affectedId, req);
    }

    // COMPLETE
    @PostMapping("/{affectedId}/complete")
    public AffectedVehicleResponse complete(@PathVariable Long campaignId,
                                            @PathVariable Long affectedId,
                                            @Valid @RequestBody AffectedVehicleCompleteRequest req) {
        return service.markCompleted(affectedId, req);
    }

    // NOTIFY
    @PostMapping("/{affectedId}/notify")
    public AffectedVehicleResponse notify(@PathVariable Long campaignId,
                                          @PathVariable Long affectedId) {
        return service.markNotified(affectedId);
    }
}