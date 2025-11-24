package com.oem.evuser.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.oem.evuser.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long userId;
    private String username;
    private String password;
    private String email;
    private String phone;
    private UserStatus status;
    private Long serviceCenterId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLogin;

    private Set<RoleDto> roles;
}
