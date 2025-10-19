package com.oem.evwarranty.controller;

import com.oem.evwarranty.dto.request.CampaignCreateRequest;
import com.oem.evwarranty.dto.request.CampaignUpdateRequest;
import com.oem.evwarranty.dto.response.CampaignResponse;
import com.oem.evwarranty.model.enums.CampaignStatus;
import com.oem.evwarranty.model.enums.CampaignType;
import com.oem.evwarranty.service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService service;

    public CampaignController(CampaignService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CampaignCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public CampaignResponse update(@PathVariable Integer id,
                                   @Valid @RequestBody CampaignUpdateRequest req) {
        return service.update(id, req);
    }

    @GetMapping("/{id}")
    public CampaignResponse get(@PathVariable Integer id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public Page<CampaignResponse> search(@RequestParam(required = false) String code,
                                         @RequestParam(required = false) CampaignStatus status,
                                         @RequestParam(required = false) CampaignType type,
                                         Pageable pageable) {
        return service.search(code, status, type, pageable);
    }
}
