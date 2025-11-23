package com.oem.evcampaign.controller;

import com.oem.evcampaign.config.ApiConstants;
import com.oem.evcampaign.dto.request.NotificationCreateRequest;
import com.oem.evcampaign.dto.response.NotificationResponse;
import com.oem.evcampaign.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    public NotificationResponse create(@Valid @RequestBody NotificationCreateRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public NotificationResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/by-campaign/{campaignId}")
    public Page<NotificationResponse> listByCampaign(@PathVariable Long campaignId, Pageable pageable) {
        return service.listByCampaign(campaignId, pageable);
    }

    @GetMapping("/by-affected/{affectedId}")
    public Page<NotificationResponse> listByAffected(@PathVariable Long affectedId, Pageable pageable) {
        return service.listByAffected(affectedId, pageable);
    }
}