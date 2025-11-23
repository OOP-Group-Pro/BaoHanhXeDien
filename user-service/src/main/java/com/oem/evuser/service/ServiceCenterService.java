package com.oem.evuser.service;

import com.oem.evuser.entity.ServiceCenter;
import com.oem.evuser.repository.ServiceCenterRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceCenterService {

    private final ServiceCenterRepository repository;

    public ServiceCenterService(ServiceCenterRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "service_centers_list") // Cache toàn bộ danh sách
    public List<ServiceCenter> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "service_centers", key = "#id") // Cache từng trung tâm
    public ServiceCenter getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    @CacheEvict(value = "service_centers_list", allEntries = true)
    public ServiceCenter create(ServiceCenter sc) {
        return repository.save(sc);
    }

    @Transactional
    @CacheEvict(value = {"service_centers", "service_centers_list"}, allEntries = true) // Xóa cache cũ
    public ServiceCenter update(Long id, ServiceCenter sc) {
        ServiceCenter existing = repository.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setCenterName(sc.getCenterName());
        existing.setCenterAddress(sc.getCenterAddress());
        return repository.save(existing);
    }

    @Transactional
    @CacheEvict(value = {"service_centers", "service_centers_list"}, allEntries = true)
    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

}