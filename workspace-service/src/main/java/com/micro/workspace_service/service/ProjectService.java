package com.micro.workspace_service.service;

import com.micro.common_lib.enums.ProjectPermission;
import com.micro.workspace_service.dto.ProjectRequest;
import com.micro.workspace_service.dto.ProjectResponse;
import com.micro.workspace_service.dto.ProjectSummayResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectSummayResponse> getUserProjects();

    List<ProjectResponse> getProjectById(Long userid, Long projectId);

    ProjectResponse createProject(ProjectRequest request);

    ProjectResponse updateProject(Long id, ProjectRequest request);

    ProjectSummayResponse getUserProjectById(Long id);

    void deleteProjectById(Long id);

    void softDelete(Long id);

    boolean hasPermission(Long projectId, ProjectPermission permission);
}
