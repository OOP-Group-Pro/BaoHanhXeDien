package com.oem.evpart.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Order(1) // Chạy đầu tiên để bắt Key sớm nhất
public class InternalAuthFilter extends OncePerRequestFilter {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";
    private static final String USER_ID_HEADER = "X-User-ID";

    // 🛠️ HARDCODE KEY Ở ĐÂY ĐỂ ĐẢM BẢO KHÔNG LỖI BIẾN MÔI TRƯỜNG
    private final String internalSecretKey = "we_dont_talk_anymore";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Bỏ qua các API Auth (Login/Register)
        if (request.getRequestURI().contains("/api/v1/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Lấy Header
        String requestSecret = request.getHeader(INTERNAL_SECRET_HEADER);
        String userId = request.getHeader(USER_ID_HEADER);

        // LOG DEBUG: Để xem Part Service nhận được cái gì
        System.out.println(">>> [FILTER CHECK] URL: " + request.getRequestURI());
        System.out.println(">>> [FILTER CHECK] Secret: " + requestSecret);
        System.out.println(">>> [FILTER CHECK] UserID: " + userId);

        // 3. LOGIC XÁC THỰC

        // TRƯỜNG HỢP A: GỌI NỘI BỘ (Có Secret Key đúng)
        if (requestSecret != null && requestSecret.equals(internalSecretKey)) {
            // ✅ Cấp quyền ADMIN ngay lập tức
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "internal-service",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("✅ INTERNAL AUTH: Granted ROLE_ADMIN");
        }
        // TRƯỜNG HỢP B: GỌI TỪ NGƯỜI DÙNG (Không có Secret, nhưng có UserID từ Gateway)
        else if (userId != null) {
            // ✅ Cho qua, để GatewayAuthFilter xử lý tiếp việc lấy thông tin User
            System.out.println("✅ USER AUTH: Passed to next filter");
        }
        // TRƯỜNG HỢP C: KHÔNG CÓ GÌ CẢ (Truy cập trái phép)
        else {
            System.err.println("❌ ACCESS DENIED: Missing both Secret Key and UserID");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Access Forbidden: Invalid Internal Key or Missing User Context.");
            return; // Dừng lại, không cho đi tiếp
        }

        // 4. Cho phép đi tiếp (vào Controller hoặc Filter tiếp theo)
        filterChain.doFilter(request, response);
    }
}