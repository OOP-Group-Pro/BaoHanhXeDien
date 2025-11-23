package com.oem.evcampaign.controller;

import com.oem.evcampaign.config.ApiConstants;
import com.oem.evcampaign.dto.request.AppointmentCompleteRequest;
import com.oem.evcampaign.dto.request.AppointmentCreateRequest;
import com.oem.evcampaign.dto.request.AppointmentRescheduleRequest;
import com.oem.evcampaign.dto.request.*;
import com.oem.evcampaign.dto.response.AppointmentResponse;
import com.oem.evcampaign.dto.response.PageCacheDto;
import com.oem.evcampaign.model.enums.AppointmentStatus;
import com.oem.evcampaign.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/appointments")

public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping
    public AppointmentResponse create(@Valid @RequestBody AppointmentCreateRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public AppointmentResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/reschedule")
    public AppointmentResponse reschedule(@PathVariable Long id,
                                          @Valid @RequestBody AppointmentRescheduleRequest req) {
        return service.reschedule(id, req);
    }

    @PostMapping("/{id}/complete")
    public AppointmentResponse complete(@PathVariable Long id,
                                        @Valid @RequestBody AppointmentCompleteRequest req) {
        return service.complete(id, req);
    }

    @GetMapping("/by-campaign/{campaignId}")
    public PageCacheDto<AppointmentResponse> listByCampaign(@PathVariable Long campaignId,
                                                            @RequestParam(required = false) AppointmentStatus status,
                                                            Pageable pageable) {
        return service.listByCampaign(campaignId, status, pageable);
    }

    @GetMapping("/by-affected/{affectedId}")
    public PageCacheDto<AppointmentResponse> listByAffected(@PathVariable Long affectedId,
                                                    Pageable pageable) {
        return service.listByAffected(affectedId, pageable);
    }
}
