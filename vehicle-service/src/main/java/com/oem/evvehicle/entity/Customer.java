package com.oem.evvehicle.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    // [THÊM MỚI] ID tham chiếu sang User Service (Quan trọng cho đồng bộ)
    @Column(name = "user_id", unique = true)
    private Long userId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "phone_num")
    private String phoneNum;

    @Column(name = "email", unique = true)
    private String email;
}