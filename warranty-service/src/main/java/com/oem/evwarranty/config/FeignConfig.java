package com.oem.evwarranty.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Value("${internal.secret-key:we_dont_talk_anymore}")
    private String internalSecretKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // In log ra để xem nó có chạy vào đây không
            System.out.println(">>> 🚀 FEIGN INTERCEPTOR: Adding Secret Key to Header");

            // Gán cứng key "we_dont_talk_anymore" để test
            requestTemplate.header("X-Internal-Secret", "we_dont_talk_anymore");
        };
    }
}