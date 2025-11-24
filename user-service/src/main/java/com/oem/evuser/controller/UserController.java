package com.oem.evuser.controller;

import com.oem.evuser.dto.UserDto;
import com.oem.evuser.dto.response.PageCacheDto;
import com.oem.evuser.entity.User;
import com.oem.evuser.security.UserDetailsPrincipal;
import com.oem.evuser.service.UserService;
import com.oem.evuser.dto.response.UserResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    // --- CREATE ---
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/{id}")
    public ResponseEntity<User> getUserFull(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return (user != null) ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public PageCacheDto<UserDto> listAllUsersFull(Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/admin/{roleName}")
    public ResponseEntity<User> createUser(@RequestBody User user, @PathVariable String roleName) {
        User created = userService.createUser(user, roleName);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/admin/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return (updated != null) ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok().body(userService.getUserInfoById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EVM_STAFF', 'ROLE_SC_STAFF', 'ROLE_SC_TECHNICIAN')")
    public ResponseEntity<List<UserResponseDto>> findUsersByCriteria (
            @RequestParam String roleName,
            Authentication authentication
    ) {
        UserDetailsPrincipal principal = (UserDetailsPrincipal) authentication.getPrincipal();
        Long centerId = null;

        // ⬇️ SỬA: Cho phép cả ADMIN và EVM_STAFF xem toàn cục ⬇️
        boolean isGlobalUser = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_EVM_STAFF"));

        if (!isGlobalUser) {
            // Neu khong phai Admin ma khong co centerId la loi du lieu nen thoat
            if (principal.getCenterId() == null ) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else {
                centerId = principal.getCenterId();
            }
        }

        return ResponseEntity.ok().body(userService.findUsersByCriteria(roleName, centerId));

    }

    // For Warranty service:
    @PostMapping("/details-map")
    public ResponseEntity<Map<Long, UserResponseDto>> getUsersDetailsByIds(@RequestBody List<Long> userIds) {
        Map<Long, UserResponseDto> userMap = userService.findUsersByIds(userIds);
        return ResponseEntity.ok(userMap);
    }
}
