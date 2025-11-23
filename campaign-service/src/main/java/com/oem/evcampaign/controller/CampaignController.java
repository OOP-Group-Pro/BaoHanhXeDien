package com.oem.evcampaign.controller;

import com.oem.evcampaign.config.ApiConstants;
import com.oem.evcampaign.dto.request.CampaignCreateRequest;
import com.oem.evcampaign.dto.request.CampaignUpdateRequest;
import com.oem.evcampaign.dto.response.CampaignResponse;
import com.oem.evcampaign.dto.response.PageCacheDto;
import com.oem.evcampaign.model.enums.CampaignStatus;
import com.oem.evcampaign.model.enums.CampaignType;
import com.oem.evcampaign.service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/campaigns")
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
    public CampaignResponse update(@PathVariable Long id,
                                   @Valid @RequestBody CampaignUpdateRequest req) {
        return service.update(id, req);
    }

    @GetMapping("/{id}")
    public CampaignResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public PageCacheDto<CampaignResponse> search(@RequestParam(required = false) String code,
                                                 @RequestParam(required = false) CampaignStatus status,
                                                 @RequestParam(required = false) CampaignType type,
                                                 Pageable pageable) {
        return service.search(code, status, type, pageable);
    }
}
