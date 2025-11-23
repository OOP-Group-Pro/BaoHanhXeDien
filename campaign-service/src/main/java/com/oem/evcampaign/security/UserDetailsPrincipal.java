package com.oem.evcampaign.security;

public class UserDetailsPrincipal {
    private Long userId;
    private Long centerId;

    public UserDetailsPrincipal(Long userId, Long centerId) {
        this.userId = userId;
        this.centerId = centerId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCenterId() {
        return centerId;
    }
}
