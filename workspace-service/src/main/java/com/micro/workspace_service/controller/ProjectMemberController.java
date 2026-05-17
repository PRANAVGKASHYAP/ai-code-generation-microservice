package com.micro.workspace_service.controller;

import com.micro.workspace_service.dto.InviteMemberRequest;
import com.micro.workspace_service.dto.ProjectMemberResponse;
import com.micro.workspace_service.dto.UpdateRoleRequest;
import com.micro.workspace_service.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @GetMapping
    public ResponseEntity<List<ProjectMemberResponse>>getAllMembers(@PathVariable Long projectId){
        Long userId = 1L;
        return ResponseEntity.ok(projectMemberService.getAllMembers(projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectMemberResponse>inviteMember(@PathVariable Long projectId , @RequestBody @Valid InviteMemberRequest request){
        Long userId = 1L;
        return ResponseEntity.status(HttpStatus.CREATED).body(projectMemberService.inviteMember(projectId  , request));
    }

    @PatchMapping("/{memberId}")
    ResponseEntity<ProjectMemberResponse>updateMemberRole(@PathVariable Long projectId , @PathVariable Long memberId , @RequestBody @Valid  UpdateRoleRequest request ){
        Long userId = 1L;
        return ResponseEntity.ok(projectMemberService.updateMemberRole(projectId , memberId , request));
    }

    @DeleteMapping("/{memberId}")
    ResponseEntity<Void>deleteMember(@PathVariable Long projectId , @PathVariable Long memberId){
        Long userId = 1L;
        projectMemberService.deleteMember(projectId , memberId );
        return ResponseEntity.noContent().build();
    }
}
