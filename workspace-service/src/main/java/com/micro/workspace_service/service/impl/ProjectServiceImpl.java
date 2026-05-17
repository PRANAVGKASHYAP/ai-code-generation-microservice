package com.micro.workspace_service.service.impl;


import com.micro.common_lib.DTO.PlanDTO;
import com.micro.common_lib.DTO.UserDTO;
import com.micro.common_lib.enums.ProjectPermission;
import com.micro.common_lib.enums.ProjectRole;
import com.micro.common_lib.error.BadRequestException;
import com.micro.common_lib.error.ResourceNotFoundException;
import com.micro.common_lib.security.AuthUtil;
import com.micro.workspace_service.client.AccountServiceClient;
import com.micro.workspace_service.dto.ProjectRequest;
import com.micro.workspace_service.dto.ProjectResponse;
import com.micro.workspace_service.dto.ProjectSummayResponse;
import com.micro.workspace_service.entity.Project;
import com.micro.workspace_service.entity.ProjectMember;
import com.micro.workspace_service.entity.ProjectMemberId;
import com.micro.workspace_service.mapper.ProjectMapper;
import com.micro.workspace_service.repository.ProjectMemberRepository;
import com.micro.workspace_service.repository.ProjectRepository;
import com.micro.workspace_service.service.ProjectService;
import com.micro.workspace_service.service.ProjectTemplateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {
    //injecting the repository
    @Autowired
    private ProjectRepository projectRepository;

//    @Autowired
//    private UserRepository userRepository;

    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private ProjectMemberRepository projectMemberRepository;
    @Autowired
    private AuthUtil jwtUtil;
//    @Autowired
//    private SubscriptionService subscriptionService;
    @Autowired
    AccountServiceClient accountServiceClient;
    @Autowired
    private ProjectTemplateService projectTemplateService; // whenever a new project gets created , this service will be called to copy the starter template

    @Override
    public List<ProjectSummayResponse> getUserProjects() {

//        return projectRepository.findAllAccessibleByUser(jwtUtil.getCurrentUserId())
//                .stream()
//                .map(proj ->
//                {
//                    projectMapper.toProjectSummaryResponse(proj.getProject() , proj.getRole());
//                }
//                )
//                .toList();
        Long userId = jwtUtil.getCurrentUserId();
        List<ProjectRepository.ProjectWithRole>projects = projectRepository.findAllAccessibleByUser(userId);
        return projects.stream()
                .map(
                        ele -> projectMapper.toProjectSummaryResponse(ele.getProject() , ele.getRole())
                ).toList();

    }

    @Override
    public List<ProjectResponse> getProjectById(Long userid, Long projectId) {
        return List.of();
    }

    @Override
    public ProjectResponse createProject(ProjectRequest request) {

        // we can use the subscription service to limit the creation of the projects
        if(!canCreateProject()){
            throw  new BadRequestException("The user cannot create more projects with the current plan");
        }
        Long userId = jwtUtil.getCurrentUserId();
        UserDTO currUser = accountServiceClient.getUserById(userId);
        //User curr = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User is not found with id " + userId));
        Project newProj = Project.builder()
                .name(request.name())
                .build();
        projectRepository.save(newProj);

        // make the user requesting to owner
        ProjectMemberId ownerId = new ProjectMemberId(newProj.getId() , userId);
        ProjectMember owner = ProjectMember.builder()
                .id(ownerId)
                .role(ProjectRole.OWNER)
                .project(newProj)
                .build();
        projectMemberRepository.save(owner);
        // here copy the template to this new project
        projectTemplateService.generateStarterTemplate(newProj.getId());
        return projectMapper.toProjectResponse(newProj);
    }

    @Override
    @PreAuthorize("@security.canEditProject(#id)")
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project proj = projectRepository.findAccessibleProjectById(id , jwtUtil.getCurrentUserId()).orElseThrow();
        proj.setName(request.name());
        proj = projectRepository.save(proj);
        return projectMapper.toProjectResponse(proj);
    }

    @Override
    @PreAuthorize("@security.canViewProject(#id)")
    public ProjectSummayResponse getUserProjectById(Long id) {
        Long userId = jwtUtil.getCurrentUserId();
        ProjectRepository.ProjectWithRole projectWithRole = projectRepository.findAccessibleProjectByIdWithRole(id , userId).orElseThrow(
                () -> new ResourceNotFoundException("Project with id" + id + "not found")
        );

//        Project proj = projectRepository.findAccessibleProjectById(id , jwtUtil.getCurrentUserId()).orElseThrow(
//                () -> new ResourceNotFoundException("The resource project with id : " + id + " Is not found")
//        );
//
//        ProjectResponse res = projectMapper.toProjectResponse(proj);
//        return res;

        ProjectSummayResponse projectSummayResponse = projectMapper.toProjectSummaryResponse(projectWithRole.getProject() , projectWithRole.getRole());

        return projectSummayResponse;
    }

    @Override
    public void deleteProjectById(Long id) {

    }

    @Override
    @PreAuthorize("@security.canDeleteProject(id)")
    public void softDelete(Long id) {
        Project proj = projectRepository.findAccessibleProjectById(id , jwtUtil.getCurrentUserId()).orElseThrow();

        proj.setDeletedAt(Instant.now());
        projectRepository.save(proj);

    }

    @Override
    public boolean hasPermission(Long projectId, ProjectPermission permission) {
        Long userId = jwtUtil.getCurrentUserId();

        Optional<ProjectRole> role = projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId);
        if(role.isEmpty()){
            return false;
        }

        return role.get().getPermissions().contains(permission);

    }


    // helper functions

    public Boolean canCreateProject(){
        Long userId = jwtUtil.getCurrentUserId();
        if (userId == null){
            return false;
        }

        PlanDTO currSubPlan = accountServiceClient.getCurrentPlan();
        if (currSubPlan == null){
            return false;
        }

        int maxAllowedProjects = currSubPlan.maxProjects();
        int currentOwnedProjects = projectMemberRepository.countProjectsOwnedByUser(userId);

        return currentOwnedProjects < maxAllowedProjects;
    }
}
