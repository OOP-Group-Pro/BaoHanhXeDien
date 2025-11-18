package com.oem.evvehicle.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "technicians")
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "technician_id")
    private Long technicianId;

    @Column(name = "technician_name", nullable = false)
    private String technicianName;

    @Column(name = "technician_level")
    private String technicianLevel;

    @Column(name = "phone_num")
    private String phoneNum;

    // Note: status is a simple String for now
    @Column(name = "status")
    private String status;
}