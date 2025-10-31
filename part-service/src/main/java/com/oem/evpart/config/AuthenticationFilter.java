package com.oem.evpart.config;

import com.fasterxml.jackson.databind.ObjectMapper; // Cần import
import com.oem.evpart.client.UserServiceClient;
import com.oem.evpart.client.dto.UserResponseDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j // Thêm log để dễ debug
public class AuthenticationFilter extends OncePerRequestFilter {

    private final UserServiceClient userServiceClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");

        // 1. Nếu không có Header
        if (userId == null || userId.isEmpty()) {
            log.warn("Request đến {} không có X-User-Id. Chuyển tiếp cho SecurityConfig xử lý.", request.getRequestURI());
            // Cứ cho đi tiếp, SecurityConfig (ở bước sau) sẽ quyết định
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Nếu CÓ Header, chúng ta BẮT BUỘC phải xác thực nó
        try {
            log.debug("Đang xác thực User ID: {}", userId);
            // Kích hoạt Feign Client gọi sang User-Service (8001)
            UserResponseDTO user = userServiceClient.getUser(Long.parseLong(userId));

            if (user != null && user.getRoles() != null) {
                // 3. TẠO AUTHENTICATION THÀNH CÔNG
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        null,
                        user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Xác thực thành công cho User ID: {}, Username: {}", userId, user.getUsername());

                // 4. Cho đi tiếp VÌ ĐÃ HỢP LỆ
                filterChain.doFilter(request, response);

            } else {
                // Trường hợp hiếm: User-Service trả về 200 OK nhưng body null
                throw new Exception("Thông tin User không hợp lệ (null)");
            }

        } catch (Exception e) {
            // 5. BẮT LỖI (VÍ DỤ: 404 NOT FOUND KHI ID KHÔNG TỒN TẠI)
            log.error("Xác thực thất bại cho User ID {}: {}", userId, e.getMessage());

            // Xóa mọi thông tin xác thực
            SecurityContextHolder.clearContext();

            // Trả về lỗi 401 Unauthorized ngay lập tức
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, String> error = Map.of(
                    "error", "Unauthorized",
                    "message", "X-User-Id không hợp lệ hoặc không tồn tại: " + userId,
                    "path", request.getRequestURI()
            );

            response.getWriter().write(objectMapper.writeValueAsString(error));

            // DỪNG LẠI TẠI ĐÂY, KHÔNG cho request đi tiếp
            return;
        }
    }
}

