package com.oem.evvehicle.dto.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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