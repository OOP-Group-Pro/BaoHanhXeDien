package com.oem.evuser.service;

import com.oem.evuser.entity.CustomUserDetails;
import com.oem.evuser.entity.User;
import com.oem.evuser.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var authorities = user.getRoles().stream()
                .flatMap(role -> {
                    var roleAuth = new SimpleGrantedAuthority("ROLE_" + role.getRoleName());
                    var permAuth = role.getPermissions().stream()
                            .map(permission -> new SimpleGrantedAuthority(permission.getCode()));
                    return Stream.concat(Stream.of(roleAuth), permAuth);
                })
                .toList();

        System.out.println("🔑 [LOGIN/AUTH] Load User: " + username); // Log sẽ ít xuất hiện hơn nhờ Cache

        return new CustomUserDetails(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getServiceCenterId(),
                authorities
        );
    }
}