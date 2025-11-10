package com.oem.evwarranty.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthGatewayFilter implements GlobalFilter, Ordered {

    // Đọc Khóa JWT từ application.yml (đã được map từ biến môi trường Docker Compose)
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Các API công khai KHÔNG cần JWT (Login, Register)
    private static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Bỏ qua API công khai
        if (OPEN_API_ENDPOINTS.stream().anyMatch(path::equals)) {
            return chain.filter(exchange);
        }

        // 2. Kiểm tra Header Authorization
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return this.onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7); // Bỏ "Bearer "

        try {
            // 3. Giải mã và Xác thực Token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecret.getBytes()) // Sử dụng secret key để xác minh
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Lấy thông tin người dùng và Roles từ claims
            String userId = claims.getSubject(); // Giả định subject là userId hoặc username
            List<String> roles = claims.get("roles", List.class);
            String centerId = claims.get("centerId", String.class);

            // 4. Thêm thông tin người dùng vào Header
            ServerHttpRequest modifiedRequest = request.mutate()
                    .headers(httpHeaders -> {
                        httpHeaders.remove("Cookie"); // Xóa Cookie
                        httpHeaders.remove("Authorization"); // Xóa Token JWT (Không cần thiết cho service nội bộ)
                    })
                    .header("X-User-ID", userId)
                    .header("X-User-Role", String.join(",", roles)) // Truyền Roles qua Header
                    .header("X-USER-CENTER-ID", centerId)
                    .build();

            // 5. Chuyển tiếp Request đã được xác thực
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            // Lỗi JWT (hết hạn, chữ ký sai, ...)
            return this.onError(exchange, "JWT validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    // Xử lý lỗi
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    // Đặt thứ tự Filter, phải chạy sớm nhất để xác thực
    @Override
    public int getOrder() {
        return -1;
    }
}