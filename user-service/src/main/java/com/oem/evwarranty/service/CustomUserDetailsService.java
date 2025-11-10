package com.oem.evwarranty.service;


import com.oem.evwarranty.entity.CustomUserDetails;
import com.oem.evwarranty.entity.User;
import com.oem.evwarranty.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("❌ User not found: " + username));

        var authorities = user.getRoles().stream()
                .flatMap(role -> {
                    // map role
                    var roleAuth = new SimpleGrantedAuthority("ROLE_" + role.getRoleName());

                    // map permissions nếu có
                    var permAuth = role.getPermissions().stream()
                            .map(permission -> new SimpleGrantedAuthority(permission.getCode()));

                    // kết hợp role + permissions
                    return Stream.concat(Stream.of(roleAuth), permAuth);
                })
                .toList();

        // 👉 Debug in ra console để kiểm tra token đang có quyền gì
        System.out.println("🔑 [LOGIN] User: " + username);
        System.out.println("🧩 Authorities: " + authorities);

        return new CustomUserDetails(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getServiceCenterId(), // ⬅️ Thêm centerId
                authorities
        );
    }
}
