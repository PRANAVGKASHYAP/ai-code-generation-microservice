package com.micro.workspace_service.service;




import com.micro.workspace_service.dto.InviteMemberRequest;
import com.micro.workspace_service.dto.ProjectMemberResponse;
import com.micro.workspace_service.dto.UpdateRoleRequest;

import java.util.List;

public interface ProjectMemberService {
    List<ProjectMemberResponse> getAllMembers(Long projectId );

    ProjectMemberResponse inviteMember(Long projectId,  InviteMemberRequest request);

    ProjectMemberResponse updateMemberRole(Long projectId, Long memberId, UpdateRoleRequest request);

    Void deleteMember(Long projectId, Long memberId );
}
