package com.oem.evwarranty.service;

import com.oem.evwarranty.entity.ServiceCenter;
import com.oem.evwarranty.repository.ServiceCenterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceCenterService {

    private final ServiceCenterRepository repository;

    public ServiceCenterService(ServiceCenterRepository repository) {
        this.repository = repository;
    }

    public List<ServiceCenter> getAll() {
        return repository.findAll();
    }

    public ServiceCenter getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public ServiceCenter create(ServiceCenter sc) {
        return repository.save(sc);
    }

    public ServiceCenter update(Long id, ServiceCenter sc) {
        ServiceCenter existing = repository.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setCenterName(sc.getCenterName());
        existing.setCenterAddress(sc.getCenterAddress());
        return repository.save(existing);
    }

    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

}
