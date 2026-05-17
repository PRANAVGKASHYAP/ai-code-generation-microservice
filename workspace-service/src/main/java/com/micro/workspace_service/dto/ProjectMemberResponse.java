package com.micro.workspace_service.dto;


import com.micro.common_lib.enums.ProjectRole;

import java.time.Instant;

public record ProjectMemberResponse(String name , String username , Long userId , ProjectRole role , Instant invitedAt) {
}
