package com.micro.workspace_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectRequest(@NotNull @NotBlank String name ) {
}
