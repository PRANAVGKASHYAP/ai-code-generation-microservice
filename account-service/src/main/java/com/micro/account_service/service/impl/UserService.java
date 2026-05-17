package com.micro.account_service.service.impl;

import com.micro.account_service.entity.User;
import com.micro.account_service.repository.UserRepository;
import com.micro.common_lib.security.JwtUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User not found with username: " + username)
                );
        // v need to return the object of that class which implements the user details

        // here it is the jwt user context
        return new JwtUserContext(
                user.getId(),user.getUsername() , new ArrayList<>() , user.getPassword()
        );
    }
}
