package com.micro.workspace_service.dto;

import com.micro.common_lib.enums.ProjectRole;

import java.time.Instant;

public record ProjectSummayResponse(Long id , String name, Instant createdAt , Instant updatedAt , ProjectRole role) {
}
