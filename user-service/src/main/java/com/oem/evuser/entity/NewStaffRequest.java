package com.oem.evuser.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "new_staff_requests")
@Getter
@Setter
public class NewStaffRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    private String fullName;
    private String username;
    private Long serviceCenterId;
    private String email;
    private String phone;
    @Column(name = "role_proposed")
    private String role;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "newstaffrequest_role",
            joinColumns = @JoinColumn(name = "request_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> proposedRoles = new HashSet<>();

    private String createdBy; // Manager ID
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String approvedBy; // Admin username

    public NewStaffRequest() {}
    // getters + setters
}
