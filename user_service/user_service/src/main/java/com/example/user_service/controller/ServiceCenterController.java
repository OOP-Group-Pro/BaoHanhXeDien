package com.example.user_service.controller;

import com.example.user_service.dto.response.CenterDetailsDTO;
import com.example.user_service.entity.ServiceCenter;
import com.example.user_service.service.ServiceCenterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/service-centers")
public class ServiceCenterController {

    private final ServiceCenterService serviceCenterService;

    public ServiceCenterController(ServiceCenterService serviceCenterService) {
        this.serviceCenterService = serviceCenterService;
    }

    @GetMapping
    //@PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServiceCenter>> getAll() {
        return ResponseEntity.ok(serviceCenterService.getAll());
    }

    @GetMapping("/{centerId}")
    //@PreAuthorize("isAuthenticated()")
    public ResponseEntity<CenterDetailsDTO> getCenterDetails(@PathVariable("centerId") Long centerId) {
        ServiceCenter sc = serviceCenterService.getById(centerId);
        if (sc == null) {
            return ResponseEntity.notFound().build();
        }

        CenterDetailsDTO dto = new CenterDetailsDTO(
                sc.getCenterId(),
                sc.getCenterName(),
                sc.getCenterAddress()
        );
        return ResponseEntity.ok(dto);
    }

    // 🔒 Chỉ ADMIN mới được tạo, sửa, xóa
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceCenter> create(@RequestBody ServiceCenter sc) {
        return ResponseEntity.ok(serviceCenterService.create(sc));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceCenter> update(@PathVariable Long id, @RequestBody ServiceCenter sc) {
        ServiceCenter updated = serviceCenterService.update(id, sc);
        return (updated != null) ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return serviceCenterService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
