package com.micro.workspace_service.controller;


import com.micro.workspace_service.dto.DeployResponse;
import com.micro.workspace_service.dto.ProjectRequest;
import com.micro.workspace_service.dto.ProjectResponse;
import com.micro.workspace_service.dto.ProjectSummayResponse;
import com.micro.workspace_service.service.DeploymentService;
import com.micro.workspace_service.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final DeploymentService deploymentService;

    //adding all the mappings
    @GetMapping
    public ResponseEntity<List<ProjectSummayResponse>>getMyProjects(){
        return ResponseEntity.ok(projectService.getUserProjects());
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectSummayResponse>getProjectById(@PathVariable Long projectId){
        Long dummyUser = 1L;
        return ResponseEntity.ok(projectService.getUserProjectById(projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody @Valid  ProjectRequest request){
        Long userId = 1L;
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request ));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id , @RequestBody @Valid ProjectRequest request){
        Long userId =1L;
        return ResponseEntity.ok(projectService.updateProject(id , request ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id){
        Long userId = 1L;
        projectService.softDelete(id);
        return null;
    }

    @PostMapping("/{id}/deploy")
    public ResponseEntity<DeployResponse> deployProject(@PathVariable Long id){
        log.info("deploying....");
        return  ResponseEntity.ok(deploymentService.deploy(id));
    }
}
