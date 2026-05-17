package com.micro.account_service.service.impl;


import com.micro.account_service.dto.auth.AuthResponse;
import com.micro.account_service.dto.auth.LogInRequest;
import com.micro.account_service.dto.auth.SignUpRequest;
import com.micro.account_service.dto.auth.UserProfileResponse;
import com.micro.account_service.entity.User;
import com.micro.account_service.mapper.UserMapper;
import com.micro.account_service.repository.UserRepository;
import com.micro.account_service.service.AuthService;
import com.micro.common_lib.DTO.UserDTO;
import com.micro.common_lib.error.BadRequestException;
import com.micro.common_lib.security.AuthUtil;
import com.micro.common_lib.security.JwtUserContext;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public AuthResponse signup(SignUpRequest request) {
        // validate the request , save the new user into the database
        String name = request.name();
        String email = request.username();
        String rawPassword = request.password();

        Optional<User> curr = userRepository.findByUsername(email);
        if(curr.isPresent()){
            throw new BadRequestException("User aldready exists with the name " + name + "Email" + email);
        }

        User newUser = userMapper.signUpReqToUser(request);
        //before saving hash the password
        String hash = encoder.encode(rawPassword);
        newUser.setPassword(hash);
        newUser = userRepository.save(newUser);

        // now mapping the user to jwt user principle
        JwtUserContext jwtUser = new JwtUserContext(newUser.getId(),  newUser.getUsername() , new ArrayList<>() , null);

        //generate the jwt token and then send
        //UserProfileResponse response = userMapper.userToUserProfileResponse(newUser);

        // use a mapper to convert eh user object to user dto
        UserDTO newUserDTO = userMapper.UserToUserDTO(newUser);
        return new AuthResponse(  authUtil.generateAccessToken(jwtUser) , userMapper.jwtProncipleToUserProfileResponse(jwtUser));
    }

    @Override
    public AuthResponse login(LogInRequest request) {

        //1 . first just do username and password check
        Authentication authObj = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username() , request.password())
        );
        //User user = (User)authObj.getPrincipal();
        JwtUserContext jwtUser = (JwtUserContext)authObj.getPrincipal();
        //UserProfileResponse response = userMapper.userToUserProfileResponse(user);
        //now generate the token
        //UserDTO userDTO = userMapper.UserToUserDTO();
        String jwtToken = authUtil.generateAccessToken(jwtUser);
        return new AuthResponse(jwtToken , userMapper.jwtProncipleToUserProfileResponse(jwtUser));
    }
}
