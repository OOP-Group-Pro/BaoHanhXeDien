package com.example.user_service.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO cho PartClient
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartResponse {
    private Long id;
    private String partName;
    private String partCode;
}
