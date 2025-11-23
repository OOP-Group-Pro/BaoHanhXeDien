package com.oem.evuser.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "service_centers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long centerId;

    @Column(nullable = false)
    private String centerName;

    private String centerAddress;
}
