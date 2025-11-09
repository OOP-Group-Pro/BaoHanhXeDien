package com.oem.evpart.config;

import com.oem.evpart.security.GatewayAuthFilter;
import com.oem.evpart.security.InternalAuthFilter;
import lombok.RequiredArgsConstructor;
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
@EnableMethodSecurity(prePostEnabled = true) // Cần thiết cho @PreAuthorize
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
                .cors(withDefaults()) // Kích hoạt CORS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // SỬA LẠI LUẬT:
                .authorizeHttpRequests(auth -> auth

                        // 2. BẮT BUỘC tất cả các request khác phải được xác thực
                        .anyRequest().authenticated()
                )
                // 3. Thêm Filter của chúng ta vào trước
                .addFilterBefore(internalAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Bean CORS giữ nguyên như cũ
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:5173",  // 1. Cho Vite (Dev)
                "http://oem.webhop.me"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-User-Id"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
