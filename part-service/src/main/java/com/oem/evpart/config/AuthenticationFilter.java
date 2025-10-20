// File: src/main/java/com/oem/evpart/config/security/AuthenticationFilter.java
package com.oem.evpart.config;

import com.oem.evpart.client.UserServiceClient;
import com.oem.evpart.client.dto.UserResponseDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private final UserServiceClient userServiceClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Lấy user ID từ header do API Gateway truyền vào
        String userId = request.getHeader("X-User-Id");

        if (userId != null && !userId.isEmpty()) {
            try {
                // 2. Dùng Feign Client gọi User-Service để lấy thông tin chi tiết
                UserResponseDTO user = userServiceClient.getUserById(Long.parseLong(userId));

                if (user != null) {
                    // 3. Tạo đối tượng Authentication và set vào SecurityContext
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            null, // không cần credentials
                            user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Có thể log lỗi ở đây, ví dụ: không tìm thấy user hoặc User-Service bị lỗi
                SecurityContextHolder.clearContext();
            }
        }

        // 4. Chuyển request đi tiếp trong chuỗi filter
        filterChain.doFilter(request, response);
    }
}