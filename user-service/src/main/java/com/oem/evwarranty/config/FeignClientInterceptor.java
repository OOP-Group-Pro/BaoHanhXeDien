package com.oem.evwarranty.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        var requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            System.out.println("Không có RequestContextHolder — Feign đang gọi nội bộ (background).");
            return;
        }

        var request = ((ServletRequestAttributes) requestAttributes).getRequest();
        var authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            template.header("Authorization", authHeader);
            System.out.println("Forwarded Authorization header to Feign request: " + authHeader.substring(0, 30) + "...");
        } else {
            System.out.println("Không tìm thấy Authorization header trong request gốc!");
        }
    }
}
