package com.oem.evpart.config;

import com.oem.evpart.security.GatewayAuthFilter;
import com.oem.evpart.security.InternalAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final InternalAuthFilter internalAuthFilter;
    private final GatewayAuthFilter gatewayAuthFilter;

    public SecurityConfig(InternalAuthFilter internalAuthFilter, GatewayAuthFilter gatewayAuthFilter) {
        this.internalAuthFilter = internalAuthFilter;
        this.gatewayAuthFilter = gatewayAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 🔥 QUAN TRỌNG: Cho phép API này nếu có quyền ADMIN (do InternalAuthFilter cấp)
                        .requestMatchers("/api/v1/parts/allocate-claim").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                // 🔥🔥🔥 SỬA LẠI THỨ TỰ FILTER TẠI ĐÂY 🔥🔥🔥

                // 1. Đặt GatewayAuthFilter chạy trước UsernamePassword...
                .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // 2. Đặt InternalAuthFilter chạy TRƯỚC CẢ GatewayAuthFilter (Ưu tiên số 1)
                // Logic: Kiểm tra Key nội bộ trước -> Nếu đúng thì cấp quyền -> GatewayFilter thấy có quyền rồi thì cho qua
                .addFilterBefore(internalAuthFilter, GatewayAuthFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:5173",
                "http://oem.webhop.me"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-User-Id", "X-Internal-Secret")); // Thêm header này vào CORS cho chắc
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}