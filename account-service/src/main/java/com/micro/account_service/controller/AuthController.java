package com.micro.account_service.controller;

import com.micro.account_service.dto.auth.AuthResponse;
import com.micro.account_service.dto.auth.LogInRequest;
import com.micro.account_service.dto.auth.SignUpRequest;
import com.micro.account_service.dto.auth.UserProfileResponse;
import com.micro.account_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignUpRequest request){
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> logIn(@RequestBody LogInRequest request){
        return ResponseEntity.ok(authService.login(request));
    }
}
