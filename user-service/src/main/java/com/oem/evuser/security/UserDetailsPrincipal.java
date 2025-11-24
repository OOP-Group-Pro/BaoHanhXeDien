package com.oem.evuser.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

@Getter
@AllArgsConstructor
public class UserDetailsPrincipal implements UserDetails {

    private Long userId;
    private String username; // 🔥 Có trường này
    private Long centerId;
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    @JsonIgnore
    public String getPassword() { return null; }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}