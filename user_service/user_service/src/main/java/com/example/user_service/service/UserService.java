package com.example.user_service.service;

import com.example.user_service.entity.Role;
import com.example.user_service.entity.User;
import com.example.user_service.repository.RoleRepository;
import com.example.user_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    // CREATE USER
    public User createUser(User user, String roleName) {
        // 🔧 Đổi findByRoleName → findByName
        Role role = roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    Role r = new Role(roleName);
                    return roleRepository.save(r);
                });

        // Mã hoá password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Gán role cho user
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        // Lưu user mới vào DB
        return userRepository.save(user);
    }

    // READ - Lấy user theo ID
    public User getUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    // READ - Lấy tất cả user
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    //UPDATE
    public User updateUser(int id, User user) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) return null;
        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        return userRepository.save(existing);
    }

    // DELETE user
    public boolean deleteUser(int id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
