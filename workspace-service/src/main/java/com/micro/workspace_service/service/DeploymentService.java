package com.micro.workspace_service.service;


import com.micro.workspace_service.dto.DeployResponse;

public interface DeploymentService {

    DeployResponse deploy(Long projectId);

}
