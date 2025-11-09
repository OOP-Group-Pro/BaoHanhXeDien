package com.oem.evwarranty.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id // Đánh dấu đây là khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID tự động tăng
    @Column(name = "vehicle_id")
    private Long vehicleId;

    // VIN giờ là một trường thông tin duy nhất, không phải khóa chính
    @Column(name = "vehicle_vin", length = 17, nullable = false, unique = true, updatable = false)
    private String vehicleVin;

    @Column(name = "model")
    private String model;

    @Column(name = "manufactured_date")
    private LocalDateTime manufacturedDate;

    @Column(name = "status")
    private String status;

    //Kết nối với Customers
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}