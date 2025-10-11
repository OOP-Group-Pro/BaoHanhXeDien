package com.oem.evwarranty.service;

import com.oem.evwarranty.entity.Technician;
import com.oem.evwarranty.exception.ResourceNotFoundException;
import com.oem.evwarranty.repository.TechnicianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TechnicianService {

    @Autowired
    private TechnicianRepository technicianRepository;

    public Technician createTechnician(Technician technician) {
        return technicianRepository.save(technician);
    }

    public List<Technician> getAllTechnicians() {
        return technicianRepository.findAll();
    }

    public Technician getTechnicianById(Long id) {
        return technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + id));
    }

    public void deleteTechnician(Long id) {
        if (!technicianRepository.existsById(id)) {
            throw new ResourceNotFoundException("Technician not found with ID: " + id);
        }
        technicianRepository.deleteById(id);
    }
}