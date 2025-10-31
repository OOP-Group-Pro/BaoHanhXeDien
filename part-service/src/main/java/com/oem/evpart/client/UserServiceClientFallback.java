package com.oem.evpart.client;

import com.oem.evpart.client.dto.ServiceCenterDTO;
import com.oem.evpart.client.dto.UserResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Đây là lớp dự phòng (Fallback) cho UserServiceClient.
 * Nó sẽ được tự động gọi khi User-Service (8001) bị lỗi hoặc quá tải.
 */
@Component
@Slf4j // Dùng để ghi log
public class UserServiceClientFallback implements UserServiceClient {

    /**
     * Phương thức dự phòng cho getServiceCenter.
     * Thay vì ném lỗi, nó trả về một lỗi 503 (SERVICE_UNAVAILABLE) rõ ràng.
     */
    @Override
    public ResponseEntity<ServiceCenterDTO> getServiceCenterById(Integer id) {
        log.error("Lỗi khi gọi User-Service (getServiceCenterById) cho ID: {}. Kích hoạt Fallback.", id);
        // Trả về lỗi 503 Service Unavailable (Dịch vụ không có sẵn)
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    /**
     * Phương thức dự phòng cho getUser.
     * Đây là phương thức quan trọng cho AuthenticationFilter.
     */
    @Override
    public ResponseEntity <UserResponseDTO> getUserById(Long id) {
        log.error("Lỗi khi gọi User-Service (getUser) cho ID: {}. Kích hoạt Fallback.", id);
        // Trả về null.
        // AuthenticationFilter của bạn sẽ thấy 'null' và xử lý (ví dụ: trả về 401 Unauthorized)
        // Điều quan trọng là nó không ném ra exception làm sập luồng.
        return null;
    }
}
