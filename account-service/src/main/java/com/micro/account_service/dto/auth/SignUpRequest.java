package com.micro.account_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignUpRequest(@NotBlank @NotNull String name , @Email @NotNull String username , @NotNull @NotBlank @Size(min = 6 , max = 12) String password) {
}
