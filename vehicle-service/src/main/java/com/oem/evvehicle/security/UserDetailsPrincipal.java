package com.oem.evvehicle.security;

import lombok.Getter;

@Getter
public class UserDetailsPrincipal {
    private Long userId;
    private Long centerId;

    public UserDetailsPrincipal(Long userId, Long centerId) {
        this.userId = userId;
        this.centerId = centerId;
    }

}
