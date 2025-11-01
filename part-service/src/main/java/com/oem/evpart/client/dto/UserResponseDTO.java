// File: src/main/java/com/oem/evpart/client/dto/UserResponseDTO.java
package com.oem.evpart.client.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserResponseDTO {
    private Long userId;
    private String username;
    private String email;
    private Set<String> roles; // Ví dụ: ["ROLE_ADMIN", "ROLE_SC_STAFF"]
}