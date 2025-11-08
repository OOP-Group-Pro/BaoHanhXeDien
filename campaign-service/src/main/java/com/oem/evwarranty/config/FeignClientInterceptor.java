package com.oem.evwarranty.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Value("${app.internal-secret}")
    private String internalSecretKey;

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";
    private static final String USER_ID_HEADER = "X-User-ID"; // Header chứa ID người dùng
    private static final String USER_ROLE_HEADER = "X-User-Role"; // Header chứa Roles

    @Override
    public void apply(RequestTemplate template) {

        // 1. 🌟 Thêm Header Internal Secret (BẮT BUỘC cho bảo mật nội bộ)
        if (internalSecretKey != null) {
            template.header(INTERNAL_SECRET_HEADER, internalSecretKey);
        }

        // 2. Chuyển tiếp Ngữ cảnh Người dùng (X-User-ID và X-User-Role)
        // Lấy Request gốc (nếu có, không phải là cuộc gọi nội bộ chạy ngầm)
        var requestAttributes = RequestContextHolder.getRequestAttributes();

        if (requestAttributes instanceof ServletRequestAttributes) {
            var request = ((ServletRequestAttributes) requestAttributes).getRequest();

            // Lấy header X-User-ID và X-User-Role đã được API Gateway thêm vào
            String userId = request.getHeader(USER_ID_HEADER);
            String userRoles = request.getHeader(USER_ROLE_HEADER);

            // Chuyển tiếp chúng (chúng rất nhỏ gọn và đã được xác thực)
            if (userId != null) {
                template.header(USER_ID_HEADER, userId);
            }
            if (userRoles != null) {
                template.header(USER_ROLE_HEADER, userRoles);
            }
        }
    }
}