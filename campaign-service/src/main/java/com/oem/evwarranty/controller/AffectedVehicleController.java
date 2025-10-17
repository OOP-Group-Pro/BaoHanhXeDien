package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.request.*;
import com.oem.evwarranty.dto.response.AffectedVehicleResponse;
import com.oem.evwarranty.model.enums.AffectedStatus;
import com.oem.evwarranty.service.AffectedVehicleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaigns/{campaignId}/affected-vehicles")
public class AffectedVehicleController {

    private final AffectedVehicleService service;

    public AffectedVehicleController(AffectedVehicleService service) {
        this.service = service;
    }

    @PostMapping
    public AffectedVehicleResponse create(@PathVariable Integer campaignId,
                                          @Valid @RequestBody AffectedVehicleCreateRequest req) {
        req.setCampaignId(campaignId);
        return service.create(req);
    }

    @GetMapping
    public Page<AffectedVehicleResponse> search(@PathVariable Integer campaignId,
                                                @RequestParam(required = false) String vin,
                                                @RequestParam(required = false) AffectedStatus status,
                                                Pageable pageable) {
        return service.search(campaignId, vin, status, pageable);
    }

    @GetMapping("/{affectedId}")
    public AffectedVehicleResponse get(@PathVariable Integer affectedId) {
        return service.get(affectedId);
    }

    @PutMapping("/{affectedId}")
    public AffectedVehicleResponse update(@PathVariable Integer affectedId,
                                          @RequestBody AffectedVehicleUpdateRequest req) {
        return service.update(affectedId, req);
    }

    @DeleteMapping("/{affectedId}")
    public void delete(@PathVariable Integer affectedId) {
        service.delete(affectedId);
    }

    @PostMapping("/{affectedId}/notify")
    public AffectedVehicleResponse markNotified(@PathVariable Integer affectedId) {
        return service.markNotified(affectedId);
    }

    @PostMapping("/{affectedId}/schedule")
    public AffectedVehicleResponse schedule(@PathVariable Integer affectedId,
                                            @Valid @RequestBody AffectedVehicleScheduleRequest req) {
        return service.schedule(affectedId, req);
    }

    @PostMapping("/{affectedId}/complete")
    public AffectedVehicleResponse complete(@PathVariable Integer affectedId,
                                            @Valid @RequestBody AffectedVehicleCompleteRequest req) {
        return service.markCompleted(affectedId, req);
    }
}
