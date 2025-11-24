package com.oem.evuser.service;

import com.oem.evuser.dto.UserDto;
import com.oem.evuser.dto.response.PageCacheDto;
import com.oem.evuser.entity.Role;
import com.oem.evuser.entity.User;
import com.oem.evuser.mapper.UserMapper;
import com.oem.evuser.repository.RoleRepository;
import com.oem.evuser.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import com.oem.evuser.dto.response.UserResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.oem.evuser.mapper.UserMapper.mapToUserResponseDto;

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

    // ... giữ nguyên hàm isAdmin ...
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }

    @Transactional(readOnly = true)
    // 🚀 CACHE: API này hay được các service khác gọi để lấy info hiển thị
    @Cacheable(value = "users_dto", key = "#userId")
    public UserResponseDto getBasicUserDetails(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng với ID: " + userId));

        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(user.getUserId());
        dto.setFullName(user.getUsername());
        dto.setServiceCenterId(user.getServiceCenterId());

        return dto;
    }


    // CREATE USER
    @Transactional
    @CacheEvict(value = "users_list", allEntries = true) // Xóa cache danh sách khi có user mới
    public User createUser(User user, String roleName) {
        if (!isAdmin()) {
            throw new AccessDeniedException("Chỉ ADMIN mới được phép tạo user!");
        }
        Role role = roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.addRole(role);
        return userRepository.save(user);
    }

    // READ - Lấy user theo ID
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id") // Cache thông tin chi tiết User
    public User getUserById(long id) {
        return userRepository.findById(id).orElse(null);
    }

    // READ - Lấy tất cả user
    @Transactional(readOnly = true)
   @Cacheable(value = "users_list") // Cache danh sách user (cẩn thận nếu list quá lớn)
    public PageCacheDto<UserDto> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);

        List<UserDto> dtoList = page.getContent().stream()
                .map(UserMapper::mapToUserDto)
                .toList();

        return PageCacheDto.from(page.map(UserMapper::mapToUserDto));
    }

    // UPDATE
    @Transactional
  @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),      // Xóa cache user cũ
            @CacheEvict(value = "users_dto", key = "#id"),  // Xóa cache DTO cũ
            @CacheEvict(value = "users_list", allEntries = true) // Reset list
    })
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

    // DELETE
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "users_dto", key = "#id"),
            @CacheEvict(value = "users_list", allEntries = true)
    })
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

    // Hàm này cũng hay được gọi, dùng chung cache với getBasicUserDetails nếu logic giống nhau
    @Transactional(readOnly = true)
    @Cacheable(value = "users_dto", key = "#id")
    public UserResponseDto getUserInfoById (Long id) {
        User user = userRepository.findById(id).orElseThrow( () -> new NoSuchElementException("User not found with ID: " + id) );
        return mapToUserResponseDto(user);
    }

    // ... giữ nguyên các hàm tìm kiếm khác ...
    public List<UserResponseDto> findUsersByCriteria (String roleName, Long centerId) {
        // Logic tìm kiếm phức tạp thường khó cache hiệu quả, có thể bỏ qua hoặc cache ngắn hạn
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

    public Map<Long, UserResponseDto> findUsersByIds (List<Long> ids) {
        // Hàm này lấy list, khó cache từng item trong map, nên để query trực tiếp
        List<User> users = userRepository.findAllById(ids);
        Map<Long, UserResponseDto> map = new HashMap<>();
        for (User user : users) {
            map.put(user.getUserId(), mapToUserResponseDto(user));
        }
        return map;
    }
}