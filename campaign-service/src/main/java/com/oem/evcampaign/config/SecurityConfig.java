package com.oem.evcampaign.config;

import com.oem.evcampaign.security.GatewayAuthFilter;
import com.oem.evcampaign.security.InternalAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

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
                        .anyRequest().permitAll()
                )
                .addFilterBefore(internalAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
                "http://localhost:5173",  // 1. Cho Vite (Dev)
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

