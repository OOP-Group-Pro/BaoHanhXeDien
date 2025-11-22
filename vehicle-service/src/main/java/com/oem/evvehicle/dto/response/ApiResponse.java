package com.oem.evvehicle.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // Rất quan trọng: Các trường null sẽ không bị serialize thành JSON
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data; // Dữ liệu trả về, có thể là bất cứ kiểu gì (Customer, List<Vehicle>...)

    // Private constructor để bắt buộc sử dụng static factory methods
    private ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // --- CÁC PHƯƠNG THỨC TIỆN ÍCH (STATIC FACTORY METHODS) ---

    // Dùng cho các request thành công CÓ trả về dữ liệu (GET, POST, PUT)
    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }

    // Dùng cho các request thành công KHÔNG cần trả về dữ liệu (DELETE)
    public static <T> ApiResponse<T> success(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }

    // Dùng cho các request thất bại
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}