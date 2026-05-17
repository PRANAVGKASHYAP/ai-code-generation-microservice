package com.micro.common_lib.security;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record JwtUserContext(
        Long userId , String userName , List<GrantedAuthority> authorities , String password
) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return password();
    }

    @Override
    public String getUsername() {
        return userName(); // this is the email and the name in the user
    }
}
