package com.oem.evpart.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
public class ApiResponse<T> {
    private int code;
    private String message;
    private T result;
}
