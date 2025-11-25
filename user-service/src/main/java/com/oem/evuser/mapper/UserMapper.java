package com.oem.evuser.mapper;

import com.oem.evuser.dto.UserDto;
import com.oem.evuser.dto.response.UserResponseDto;
import com.oem.evuser.entity.User;
import org.springframework.stereotype.Component;
import com.oem.evuser.dto.RoleDto;
import java.util.stream.Collectors;
import java.util.Set;

@Component
public class UserMapper {
    public static UserResponseDto mapToUserResponseDto(User user) {
        return new UserResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getServiceCenterId(),
                user.getLastLogin()
        );
    }

    public static UserDto mapToUserDto(User user) {

        return new UserDto(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                user.getServiceCenterId(),
                user.getLastLogin(),
                user.getRoles().stream()
                        .map(RoleMapper::mapToRoleDto)
                        .collect(Collectors.toSet())
        );
    }

}
