package com.oem.evpart.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(2)
public class GatewayAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal (HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader("X-USER-ID");
        String userRolesString = request.getHeader("X-USER-ROLE");

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Collection<SimpleGrantedAuthority> authorities =
                (userRolesString != null && !userRolesString.isEmpty())
                ? Arrays.stream(userRolesString.split(","))
                        .map(String::trim)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
                : List.of();

            // 4. Tạo đối tượng Authentication nhẹ
            Authentication authentication =  new UsernamePasswordAuthenticationToken(userId, null, authorities);
            // 5. Thiết lập Security Context (Đây là bước quan trọng nhất cho @PreAuthorize)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
