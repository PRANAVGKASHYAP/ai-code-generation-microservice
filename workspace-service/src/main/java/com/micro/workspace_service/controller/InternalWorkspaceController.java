package com.micro.workspace_service.controller;


import com.micro.common_lib.enums.ProjectPermission;
import com.micro.workspace_service.dto.FileContentResponse;
import com.micro.workspace_service.dto.FileTreeResponse;
import com.micro.workspace_service.service.FileService;
import com.micro.workspace_service.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/v1/projects")
@RequiredArgsConstructor
public class InternalWorkspaceController {

    private final ProjectService projectService;
    private final FileService fileService;

    @GetMapping("/{projectId}/files/tree")
    public FileTreeResponse getFileTree(@PathVariable("projectId") Long projectId) {
        return fileService.getFileTree(projectId);
    }

    @GetMapping("/{projectId}/files/content")
    public FileContentResponse getFileContent(@PathVariable("projectId") Long projectId , @RequestParam("path") String path){
        return fileService.getFile(projectId , path);
    }

    @GetMapping("/{projectId}/permissions/check")
    public boolean checkPermission(@PathVariable Long projectId , @RequestParam ProjectPermission permission){
        return projectService.hasPermission(projectId , permission);
    }
}
