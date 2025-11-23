package com.oem.evuser.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreatedEvent implements Serializable {
    private Long userId;
    private String email;
    private String role;
    private String fullName;
    private String timestamp;
}