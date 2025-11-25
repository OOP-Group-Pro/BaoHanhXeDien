package com.oem.evuser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    private String phone;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private Long serviceCenterId;

    /// Mới thêm:
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "fcm_token")
    private String fcmToken;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public User() {}

    // --- Add role với đồng bộ 2 chiều ---
    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }

    // --- Copy user ---
    public User copy() {
        User copyUser = new User();
        copyUser.setUsername(this.username + "_copy"); // bạn có thể generate tên khác
        copyUser.setPassword(this.password);           // nên hash nếu cần
        copyUser.setEmail(this.email);
        copyUser.setPhone(this.phone);
        copyUser.setStatus(this.status);
        copyUser.setServiceCenterId(this.serviceCenterId);

        // Copy roles dùng addRole để tránh lỗi Hibernate
        for (Role role : this.roles) {
            copyUser.addRole(role);
        }
        return copyUser;
    }
}
