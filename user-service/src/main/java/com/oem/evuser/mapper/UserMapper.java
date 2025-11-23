package com.oem.evuser.mapper;

import com.oem.evuser.dto.response.UserResponseDto;
import com.oem.evuser.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public static UserResponseDto mapToUserResponseDto(User user) {
        return new UserResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getServiceCenterId()
        );
    }
}
