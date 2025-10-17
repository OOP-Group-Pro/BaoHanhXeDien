package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.request.*;
import com.oem.evwarranty.dto.response.AppointmentResponse;
import com.oem.evwarranty.model.enums.AppointmentStatus;
import com.oem.evwarranty.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
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
    public AppointmentResponse get(@PathVariable Integer id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    @PostMapping("/{id}/reschedule")
    public AppointmentResponse reschedule(@PathVariable Integer id,
                                          @Valid @RequestBody AppointmentRescheduleRequest req) {
        return service.reschedule(id, req);
    }

    @PostMapping("/{id}/complete")
    public AppointmentResponse complete(@PathVariable Integer id,
                                        @Valid @RequestBody AppointmentCompleteRequest req) {
        return service.complete(id, req);
    }

    @GetMapping("/by-campaign/{campaignId}")
    public Page<AppointmentResponse> listByCampaign(@PathVariable Integer campaignId,
                                                    @RequestParam(required = false) AppointmentStatus status,
                                                    Pageable pageable) {
        return service.listByCampaign(campaignId, status, pageable);
    }

    @GetMapping("/by-affected/{affectedId}")
    public Page<AppointmentResponse> listByAffected(@PathVariable Integer affectedId,
                                                    Pageable pageable) {
        return service.listByAffected(affectedId, pageable);
    }
}
