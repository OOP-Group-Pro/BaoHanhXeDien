package com.oem.evpart.controllers;

import com.oem.evpart.dto.request.PartRequest;
import com.oem.evpart.dto.response.PartResponse;
import com.oem.evpart.services.PartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;


    @PostMapping
    public ResponseEntity<PartResponse> createPart(@Valid @RequestBody PartRequest partRequest) {
        PartResponse newPart = partService.createPart(partRequest);
        return new ResponseEntity<>(newPart, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartResponse> getPartById(@PathVariable Long id) {
        return ResponseEntity.ok(partService.getPartById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PartResponse>> getAllParts(Pageable pageable) {
        return ResponseEntity.ok(partService.getAllParts(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartResponse> updatePart(@PathVariable Long id, @Valid @RequestBody PartRequest partRequest) {
        return ResponseEntity.ok(partService.updatePart(id, partRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePart(@PathVariable Long id) {
        partService.deletePart(id);
        return ResponseEntity.noContent().build();
    }
}