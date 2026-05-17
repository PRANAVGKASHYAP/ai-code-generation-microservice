package com.micro.intellegence_service.client;

import com.micro.common_lib.enums.ProjectPermission;
import com.micro.intellegence_service.dto.FileContentResponse;
import com.micro.intellegence_service.dto.FileTreeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "workspace-service" , path = "/workspace" , url = "${WORKSPACE_SERVICE_URI:}")
public interface WorkspaceServiceClient {

    // here the client is needed to get the file contend and the file tree from the workspace service

    @GetMapping("/internal/v1/projects/{projectId}/files/tree")
    FileTreeResponse getFileTree(@PathVariable("projectId") Long projectId);

    @GetMapping("/internal/v1/projects/{projectId}/files/content")
    FileContentResponse getFileContent(@PathVariable("projectId") Long projectId, @RequestParam("path") String path);

    @GetMapping("/internal/v1/projects/{projectId}/permissions/check")
    Boolean checkPermission(@PathVariable("projectId") Long projectId, @RequestParam("permission") ProjectPermission permission);
}
