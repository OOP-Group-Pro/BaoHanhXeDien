package com.oem.evwarranty.mapper;

import com.oem.evwarranty.dto.response.UserResponseDto;
import com.oem.evwarranty.entity.User;
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
