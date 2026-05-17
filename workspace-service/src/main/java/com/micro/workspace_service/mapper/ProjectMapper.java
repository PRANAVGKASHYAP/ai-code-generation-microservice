package com.micro.workspace_service.mapper;

import com.micro.workspace_service.dto.ProjectResponse;
import com.micro.workspace_service.dto.ProjectSummayResponse;
import com.micro.workspace_service.entity.Project;
import com.micro.common_lib.enums.ProjectRole;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    //convert project entity to project response
    ProjectResponse toProjectResponse(Project project);
    ProjectSummayResponse toProjectSummaryResponse(Project project , ProjectRole role);
    //List<ProjectSummayResponse> ListToSummaryResponse(List<ProjectRepository.ProjectWithRole> projects);
}
