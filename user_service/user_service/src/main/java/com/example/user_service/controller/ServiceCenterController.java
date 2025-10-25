package com.example.user_service.controller;

import com.example.user_service.entity.ServiceCenter;
import com.example.user_service.service.ServiceCenterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service-centers")
public class ServiceCenterController {

    private final ServiceCenterService serviceCenterService;

    public ServiceCenterController(ServiceCenterService serviceCenterService) {
        this.serviceCenterService = serviceCenterService;
    }

    // Lấy tất cả
    @GetMapping
    public ResponseEntity<List<ServiceCenter>> getAll() {
        return ResponseEntity.ok(serviceCenterService.getAll());
    }

    // Lấy theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ServiceCenter> getById(@PathVariable Long id) {
        ServiceCenter sc = serviceCenterService.getById(id);
        return (sc != null) ? ResponseEntity.ok(sc) : ResponseEntity.notFound().build();
    }

    // Tạo mới
    @PostMapping
    public ResponseEntity<ServiceCenter> create(@RequestBody ServiceCenter sc) {
        return ResponseEntity.ok(serviceCenterService.create(sc));
    }

    // Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<ServiceCenter> update(@PathVariable Long id, @RequestBody ServiceCenter sc) {
        ServiceCenter updated = serviceCenterService.update(id, sc);
        return (updated != null) ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return serviceCenterService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
