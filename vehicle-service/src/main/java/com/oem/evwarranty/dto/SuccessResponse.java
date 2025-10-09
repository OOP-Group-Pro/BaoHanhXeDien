package com.oem.evwarranty.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResponse {
    private int status;
    private String message;
    private Object data;

    public SuccessResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}