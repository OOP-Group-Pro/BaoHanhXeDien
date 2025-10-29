package com.example.user_service.controller;

import com.example.user_service.entity.User;
import com.example.user_service.service.UserService;
import com.example.user_service.dto.response.UserResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET cơ bản theo ID
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserBasicInfo(@PathVariable Long userId) {
        try {
            UserResponseDto dto = userService.getBasicUserDetails(userId);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET tất cả cơ bản
    @GetMapping("")
    public ResponseEntity<List<UserResponseDto>> listBasicUsers() {
        List<UserResponseDto> dtos = userService.getAllUsers()
                .stream()
                .map(user -> {
                    UserResponseDto dto = new UserResponseDto();
                    dto.setUserId(user.getUserId());
                    dto.setFullName(user.getUsername());
                    dto.setServiceCenterId(user.getServiceCenterId());
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // ===========================
    //  Thông tin full (chỉ ADMIN)
    // ===========================

    // GET full theo ID
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{id}")
    public ResponseEntity<User> getUserFull(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return (user != null) ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    // GET tất cả full
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<User>> listAllUsersFull() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ===========================
    //  CRUD (chỉ ADMIN)
    // ===========================

    // CREATE USER
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{roleName}")
    public ResponseEntity<User> createUser(@RequestBody User user, @PathVariable String roleName) {
        User created = userService.createUser(user, roleName);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // UPDATE USER
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return (updated != null) ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // DELETE USER
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
