package com.micro.workspace_service.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "workspace-client" , path = "/workspace" , url = "${WORKSPACE_SERVICE_URI:}")
public interface WorkspaceServiceClient {

    // this is the client that will be used to call the workspace security exp method to check if user can edit a project or not
}
