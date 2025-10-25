package com.example.user_service.controller;

import com.example.user_service.entity.User;
import com.example.user_service.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.user_service.dto.ServiceCenterDTO;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- CREATE ---
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{roleName}")
    public ResponseEntity<User> createUser(@RequestBody User user, @PathVariable String roleName) {
        User created = userService.createUser(user, roleName);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // --- READ SINGLE ---
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','TECHNICIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable int id) {
        User user = userService.getUserById(id);
        return (user != null) ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    // --- READ ALL ---
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','TECHNICIAN')")
    @GetMapping
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // --- UPDATE ---
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable int id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return (updated != null) ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // --- DELETE ---
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        return userService.deleteUser(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    @GetMapping("/user/service-center/{id}")
    public ServiceCenterDTO getServiceCenterById(@PathVariable Long id) {
        // Thông thường bạn sẽ lấy dữ liệu từ DB, nhưng đây là ví dụ tạm
        return new ServiceCenterDTO(id, "Service Center " + id, "123 Example Street");
    }
}
