package com.oem.evuser.controller;

import com.oem.evuser.dto.UserDto;
import com.oem.evuser.dto.request.UserUpdateRequest;
import com.oem.evuser.dto.response.PageCacheDto;
import com.oem.evuser.entity.CustomUserDetails;
import com.oem.evuser.entity.User;
import com.oem.evuser.mapper.UserMapper;
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

    // 1. Xem hồ sơ bản thân
    // Hàm hỗ trợ: Lấy User ID từ Authentication bất chấp là class nào
    private Long extractUserId(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("Authentication is NULL");
        }

        Object principal = authentication.getPrincipal();

        // 🔥 IN LOG RA MÀN HÌNH ĐỂ SOI
        System.out.println(">>> DEBUG PRINCIPAL CLASS: " + principal.getClass().getName());
        System.out.println(">>> DEBUG PRINCIPAL DATA: " + principal.toString());

        if (principal instanceof CustomUserDetails) {
            // ⚠️ KIỂM TRA LẠI TÊN HÀM GET ID CỦA BẠN: getId() hay getUserId()?
            return ((CustomUserDetails) principal).getId();
        } else if (principal instanceof UserDetailsPrincipal) {
            return ((UserDetailsPrincipal) principal).getUserId();
        } else if (principal instanceof String) {
            // Trường hợp token lỗi hoặc cấu hình sai, principal chỉ là một chuỗi "anonymousUser"
            throw new RuntimeException("Lỗi xác thực: Principal là String (" + principal + ")");
        } else {
            throw new RuntimeException("Loại UserDetails không hỗ trợ: " + principal.getClass().getName());
        }
    }

    // 1. Xem hồ sơ bản thân
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(Authentication authentication) {
        Long userId = extractUserId(authentication); // ✅ Dùng hàm mới
        return ResponseEntity.ok(userService.getUserInfoById1(userId));
    }

    // 2. Cập nhật hồ sơ bản thân
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMyProfile(
            @RequestBody UserUpdateRequest request,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication); // ✅ Dùng hàm mới

        User updatedUser = userService.updateUserProfile(userId, request);
        UserDto responseDto = UserMapper.mapToUserDto(updatedUser);
        responseDto.setPassword(null);

        return ResponseEntity.ok(responseDto);
    }

    //Firebase nofication
    //POST /api/v1/users/fcm-token
    @PostMapping("/fcm-token")
    public ResponseEntity<String> updateFcmToken(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String token = body.get("token");
        if (token == null) return ResponseEntity.badRequest().body("Token is missing");

        // 1. Lấy User ID từ Authentication
        Object principal = authentication.getPrincipal();
        Long userId = null;

        // Kiểm tra xem Principal là Object hay String
        if (principal instanceof UserDetailsPrincipal userDetails) {
            userId = userDetails.getUserId(); // Lấy ID chuẩn
        } else if (principal instanceof String) {
            // Trường hợp dự phòng: Nếu nó là chuỗi "3"
            try {
                userId = Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid User ID");
            }
        }

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User ID not found in token");
        }

        // 2. Gọi hàm mới (Lưu theo ID)
        userService.saveFcmTokenById(userId, token);

        return ResponseEntity.ok("FCM Token saved successfully");
    }
}
