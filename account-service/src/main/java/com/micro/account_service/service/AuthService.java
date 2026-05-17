package com.micro.account_service.service;


import com.micro.account_service.dto.auth.AuthResponse;
import com.micro.account_service.dto.auth.LogInRequest;
import com.micro.account_service.dto.auth.SignUpRequest;

public interface AuthService {
    AuthResponse signup(SignUpRequest request);

    AuthResponse login(LogInRequest request);
}
