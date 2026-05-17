package com.micro.workspace_service.dto;

import com.micro.common_lib.enums.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequest(
        @NotNull
        @Email // this is a validation from spring boot jakarta
        String username ,
        @NotNull
        ProjectRole role
) {
}
