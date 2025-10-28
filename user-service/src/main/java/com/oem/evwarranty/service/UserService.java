package com.oem.evwarranty.service;


import com.oem.evwarranty.entity.Role;
import com.oem.evwarranty.entity.User;
import com.oem.evwarranty.repository.RoleRepository;
import com.oem.evwarranty.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // HÀM KIỂM TRA QUYỀN ADMIN
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }

    // CREATE USER - chỉ ADMIN được phép
    public User createUser(User user, String roleName) {
        if (!isAdmin()) {
            throw new AccessDeniedException("Chỉ ADMIN mới được phép tạo user!");
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    Role r = new Role(roleName);
                    return roleRepository.save(r);
                });

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    // READ - Lấy user theo ID (ai cũng xem được)
    public User getUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    // READ - Lấy tất cả user (ai cũng xem được)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    //  UPDATE - chỉ ADMIN được sửa
    public User updateUser(int id, User user) {
        if (!isAdmin()) {
            throw new AccessDeniedException("Chỉ ADMIN mới được phép sửa user!");
        }

        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        existing.setStatus(user.getStatus());

        return userRepository.save(existing);
    }

    // DELETE - chỉ ADMIN được xóa
    public boolean deleteUser(int id) {
        if (!isAdmin()) {
            throw new AccessDeniedException("Chỉ ADMIN mới được phép xóa user!");
        }

        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
