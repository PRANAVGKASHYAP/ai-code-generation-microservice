package com.micro.workspace_service.dto;


import java.time.Instant;

public record ProjectResponse(String name , Long id , Instant createdAt , Instant updatedAt , UserProfileResponse owner) {
}
