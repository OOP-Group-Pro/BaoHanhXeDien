package com.oem.evpart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Vô hiệu hóa CSRF (Cross-Site Request Forgery)
                .csrf(csrf -> csrf.disable())

                // Cấu hình ủy quyền
                .authorizeHttpRequests(auth -> auth
                        // Dòng quan trọng: Cho phép TẤT CẢ các request đi qua mà không cần xác thực
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}