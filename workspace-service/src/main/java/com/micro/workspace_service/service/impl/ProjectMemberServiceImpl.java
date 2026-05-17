package com.micro.workspace_service.service.impl;


import com.micro.common_lib.DTO.UserDTO;
import com.micro.common_lib.error.ResourceNotFoundException;
import com.micro.common_lib.security.AuthUtil;
import com.micro.workspace_service.client.AccountServiceClient;
import com.micro.workspace_service.dto.InviteMemberRequest;
import com.micro.workspace_service.dto.ProjectMemberResponse;
import com.micro.workspace_service.dto.UpdateRoleRequest;
import com.micro.workspace_service.entity.Project;
import com.micro.workspace_service.entity.ProjectMember;
import com.micro.workspace_service.entity.ProjectMemberId;
import com.micro.workspace_service.mapper.ProjectMemberMapper;
import com.micro.workspace_service.repository.ProjectMemberRepository;
import com.micro.workspace_service.repository.ProjectRepository;
import com.micro.workspace_service.service.ProjectMemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisSubscribedConnectionException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMemberServiceImpl implements ProjectMemberService {

    @Autowired
    ProjectMemberRepository projectMemberRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    ProjectMemberMapper projectMemberMapper;
//    @Autowired
//    private UserRepository userRepository;
    @Autowired
    AccountServiceClient accountServiceClient;

    @Autowired
    private AuthUtil jwtUtil;
    @Override
    public List<ProjectMemberResponse> getAllMembers(Long projectId) {
        Long userId = jwtUtil.getCurrentUserId();
        // get all members of this project , this api is requested by userId
        Project proj = projectRepository.findAccessibleProjectById(projectId , userId).orElseThrow();
        List<ProjectMemberResponse>members = new ArrayList<>();
        //members.add(projectMemberMapper.toProjectMemberResponseFromOwner(proj.getOwner())) ;

        //now add all the members
        members.addAll(
                projectMemberRepository.findByIdProjectId(projectId).stream()
                        .map(ele -> projectMemberMapper.toProjectMemberResponseFromMember(ele))
                        .toList()
        );

        return members;
    }

    @Override
    public ProjectMemberResponse inviteMember(Long projectId, InviteMemberRequest request) {

        Long userId = jwtUtil.getCurrentUserId();
        Project proj = projectRepository.findAccessibleProjectById(projectId , userId).orElseThrow();

        //User newUser = userRepository.findByUsername(request.username()).orElseThrow();
        UserDTO newUser = accountServiceClient.findByMailId(request.username()).orElseThrow(
                () -> new ResourceNotFoundException("user with the mail id " + request.username() + " not found")
        );
        Long newUserId = Long.parseLong(newUser.id());
        if (newUserId.equals(userId)){
            throw new RuntimeException("canot invite yourself");
        }


        ProjectMemberId newId = new ProjectMemberId(projectId , newUserId);

        if(projectMemberRepository.existsById(newId)){
            throw new RuntimeException("user adrady invited ");
        }
        ProjectMember member = new ProjectMember().builder()
                .id(newId)
                .role(request.role())
                .project(proj)
                .invitedAt(Instant.now())
                .build();
        projectMemberRepository.save(member);

        return projectMemberMapper.toProjectMemberResponseFromMember(member);
    }

    @Override
    public ProjectMemberResponse updateMemberRole(Long projectId, Long memberId, UpdateRoleRequest request) {
        Long userId = jwtUtil.getCurrentUserId();

        Project proj = projectRepository.findAccessibleProjectById(projectId , memberId).orElseThrow();

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId , memberId);
        ProjectMember member = projectMemberRepository.findById(projectMemberId).orElseThrow();
        member.setRole(request.role());
        projectMemberRepository.save(member);

        return projectMemberMapper.toProjectMemberResponseFromMember(member);
    }

    @Override
    public Void deleteMember(Long projectId, Long memberId) {

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId , memberId);
        ProjectMember member = projectMemberRepository.findById(projectMemberId).orElseThrow();

        projectMemberRepository.deleteById(projectMemberId);
        return null;
    }
}
