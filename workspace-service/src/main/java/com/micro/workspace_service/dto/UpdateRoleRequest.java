package com.micro.workspace_service.dto;

import com.micro.common_lib.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest( @NotNull ProjectRole role) {
}
