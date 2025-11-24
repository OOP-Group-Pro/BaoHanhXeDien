package com.oem.evuser.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order; // ⬅️ Quan trọng để sắp xếp thứ tự Filter
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@Order(1) // ⬅️ Đảm bảo filter này chạy đầu tiên
public class InternalAuthFilter extends OncePerRequestFilter {

    @Value("${INTERNAL_SERVICE_SECRET}")
    private String internalSecretKey;

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";
    private static final String USER_ID_HEADER = "X-User-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Kiểm tra API công khai (Đăng nhập/Đăng ký)
        if (request.getRequestURI().contains("/api/v1/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Lấy Header Nội bộ và Header Người dùng
        String internalSecretValue = request.getHeader(INTERNAL_SECRET_HEADER);
        String userIdValue = request.getHeader(USER_ID_HEADER);

        System.out.println(">>> internalSecretKey=" + internalSecretKey);
        System.out.println(">>> X-Internal-Secret header=" + internalSecretValue);

        // 3. LOGIC XÁC THỰC MỚI
        // Yêu cầu (Request) là hợp lệ nếu:
        // A. Nó là cuộc gọi nội bộ (Gateway/Service khác) VÀ có Khóa bí mật Nội bộ hợp lệ
        // HOẶC
        // B. Nó là cuộc gọi từ Client (đã qua Gateway) VÀ có Header X-User-ID (đã được Gateway xác thực)

       if (internalSecretValue != null && internalSecretValue.equals(internalSecretKey) ) {
           // Gán quyền ROLE_ADMIN cho secret, bypass PreAuthorize
           UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                   "internal-service", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
           );
           SecurityContextHolder.getContext().setAuthentication(auth);
       } else if (userIdValue == null) {
            // 4. Nếu không thỏa mãn bất kỳ điều kiện nào (Bị gọi trực tiếp từ bên ngoài mà không qua Gateway)
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            response.getWriter().write("Access Forbidden: Invalid Internal Key or Missing User Context.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}