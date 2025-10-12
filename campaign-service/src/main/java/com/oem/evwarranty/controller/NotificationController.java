package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.request.NotificationCreateRequest;
import com.oem.evwarranty.dto.response.NotificationResponse;
import com.oem.evwarranty.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
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
    public NotificationResponse get(@PathVariable Integer id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    @GetMapping("/by-campaign/{campaignId}")
    public Page<NotificationResponse> listByCampaign(@PathVariable Integer campaignId, Pageable pageable) {
        return service.listByCampaign(campaignId, pageable);
    }

    @GetMapping("/by-affected/{affectedId}")
    public Page<NotificationResponse> listByAffected(@PathVariable Integer affectedId, Pageable pageable) {
        return service.listByAffected(affectedId, pageable);
    }
}