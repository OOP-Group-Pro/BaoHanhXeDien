package com.oem.evwarranty.controller;

import com.oem.evwarranty.entity.User;
import com.oem.evwarranty.security.UserDetailsPrincipal;
import com.oem.evwarranty.service.UserService;
import com.oem.evwarranty.dto.response.UserResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- FULL ADMIN APIs ---
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{id}")
    public ResponseEntity<User> getUserFull(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return (user != null) ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<User>> listAllUsersFull() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/{roleName}")
    public ResponseEntity<User> createUser(@RequestBody User user, @PathVariable String roleName) {
        User created = userService.createUser(user, roleName);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return (updated != null) ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // --- NORMAL USER ACCESS ---
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok().body(userService.getUserInfoById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EVM_STAFF','SC_STAFF')")
    public ResponseEntity<List<UserResponseDto>> findUsersByCriteria(
            @RequestParam String roleName,
            Authentication authentication
    ) {
        UserDetailsPrincipal principal = (UserDetailsPrincipal) authentication.getPrincipal();
        Long centerId = null;

        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

            if (principal.getCenterId() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else {
                centerId = principal.getCenterId();
            }
        }

        return ResponseEntity.ok().body(userService.findUsersByCriteria(roleName, centerId));
    }

    @PostMapping("/details-map")
    public ResponseEntity<Map<Long, UserResponseDto>> getUsersDetailsByIds(@RequestBody List<Long> userIds) {
        Map<Long, UserResponseDto> userMap = userService.findUsersByIds(userIds);
        return ResponseEntity.ok(userMap);
    }
}
