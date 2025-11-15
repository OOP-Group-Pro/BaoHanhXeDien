package com.oem.evwarranty.service;


import com.oem.evwarranty.entity.Role;
import com.oem.evwarranty.entity.User;
import com.oem.evwarranty.mapper.UserMapper;
import com.oem.evwarranty.repository.RoleRepository;
import com.oem.evwarranty.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import com.oem.evwarranty.dto.response.UserResponseDto;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.oem.evwarranty.mapper.UserMapper.mapToUserResponseDto;

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

    public UserResponseDto getBasicUserDetails(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng với ID: " + userId));

        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(user.getUserId());
        dto.setFullName(user.getUsername()); // giả sử username là tên đầy đủ
        dto.setServiceCenterId(user.getServiceCenterId());

        return dto;
    }


    // CREATE USER - chỉ ADMIN được phép
    public User createUser(User user, String roleName) {
        if (!isAdmin()) {
            throw new AccessDeniedException("Chỉ ADMIN mới được phép tạo user!");
        }

        // Lấy role hoặc tạo mới nếu chưa có
        Role role = roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));

        // Mã hóa password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Thêm role với helper method để đồng bộ 2 chiều
        user.addRole(role);

        // Lưu user
        return userRepository.save(user);
    }

    // READ - Lấy user theo ID (ai cũng xem được)
    public User getUserById(long id) {
        return userRepository.findById(id).orElse(null);
    }

    // READ - Lấy tất cả user (ai cũng xem được)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // UPDATE - chỉ ADMIN được sửa
    public User updateUser(long id, User user) {
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
    public boolean deleteUser(long id) {
        if (!isAdmin()) {
            throw new AccessDeniedException("Chỉ ADMIN mới được phép xóa user!");
        }

        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public UserResponseDto getUserInfoById (Long id) {
        User user = userRepository.findById(id).orElseThrow( () -> new NoSuchElementException("User not found with ID: " + id) );
        return mapToUserResponseDto(user);
    }

    public List<UserResponseDto> findUsersByCriteria (String roleName, Long centerId) {

        // Neu centerId null => admin => chỉ lọc theo vai trò:
        if (centerId == null) {
            return userRepository.findByRoles_RoleName(roleName).stream()
                    .map(UserMapper::mapToUserResponseDto)
                    .toList();
        }else {
            return userRepository.findByRoles_RoleNameAndServiceCenterId(roleName, centerId).stream()
                    .map(UserMapper::mapToUserResponseDto)
                    .toList();
        }
    }
}
