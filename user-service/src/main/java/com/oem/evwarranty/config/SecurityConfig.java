package com.oem.evwarranty.config;

import com.oem.evwarranty.security.GatewayAuthFilter;
import com.oem.evwarranty.security.InternalAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)

public class SecurityConfig {

    private final InternalAuthFilter internalAuthFilter;
    private final GatewayAuthFilter gatewayAuthFilter;

    public SecurityConfig(InternalAuthFilter internalAuthFilter, GatewayAuthFilter gatewayAuthFilter) {
        this.internalAuthFilter = internalAuthFilter;
        this.gatewayAuthFilter = gatewayAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Auth service public
                        .requestMatchers("/api/v1/auth/**").permitAll()
                /*
                        // Feign client
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/service-centers/**").permitAll()

                        // Manager
                        .requestMatchers(HttpMethod.POST, "/api/v1/staff-requests/create").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/staff-requests/my").hasRole("MANAGER")

                        // Admin
                        .requestMatchers(HttpMethod.POST, "/api/v1/staff-requests/approve/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/staff-requests/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/staff-requests/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/staff-requests/.../reject").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/admin/**").hasRole("ADMIN")

                        // Còn lại yêu cầu auth
                        .anyRequest().authenticated()
                */
                                .anyRequest().permitAll()
                )
                .addFilterBefore(internalAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Trong TẤT CẢ các file SecurityConfig.java của backend

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // FIX: Cho phép cả 2 môi trường Dev và Production
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:63342",
                "http://localhost:5500",// 1. Cho Vite (Dev)
                "http://oem.webhop.me"      // 2. Cho Nginx (Production)
        ));

        configuration.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
