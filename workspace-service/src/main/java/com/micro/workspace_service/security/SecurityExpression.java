package com.micro.workspace_service.security;


import com.micro.common_lib.enums.ProjectPermission;
import com.micro.common_lib.enums.ProjectRole;
import com.micro.common_lib.security.AuthUtil;
import com.micro.workspace_service.entity.ProjectMemberId;
import com.micro.workspace_service.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("security")
@RequiredArgsConstructor
public class SecurityExpression {
    @Autowired
    private ProjectMemberRepository projectMemberRepository;
    @Autowired
    private AuthUtil authUtil;


    // this class is used to check the users and his roles before giving access to the projects

    public boolean canViewProject(Long projectId){
        Long userId = authUtil.getCurrentUserId();
        ProjectMemberId member = new ProjectMemberId(projectId , userId);

        Optional<ProjectRole> role = projectMemberRepository.findRoleByProjectIdAndUserId(projectId,userId);

        if(role.isPresent()){
            return true;
        }

        return false;

    }

    public boolean canEditProject(Long projectId){
        Long userId = authUtil.getCurrentUserId();
        ProjectMemberId member = new ProjectMemberId(projectId , userId);

        Optional<ProjectRole> role = projectMemberRepository.findRoleByProjectIdAndUserId(projectId,userId);

        return role.isPresent() && ( role.get().getPermissions().contains(ProjectPermission.EDIT));
    }

    public boolean canDeleteProject(Long projectId){
        Long userId = authUtil.getCurrentUserId();
        ProjectMemberId member = new ProjectMemberId(projectId , userId);

        Optional<ProjectRole> role = projectMemberRepository.findRoleByProjectIdAndUserId(projectId,userId);

        return role.isPresent() && ( role.get().getPermissions().contains(ProjectPermission.DELETE));
    }

}
