package com.micro.intellegence_service.security;

import com.micro.common_lib.enums.ProjectPermission;
import com.micro.common_lib.security.AuthUtil;
import com.micro.intellegence_service.client.WorkspaceServiceClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("security")
@RequiredArgsConstructor
@Slf4j
public class SecurityExpression {

    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private WorkspaceServiceClient workspaceServiceClient;


    public boolean hasPermission(Long projectId , ProjectPermission permission){
        // make a call to teh workspace client to get the permissions for this role
        try{
            return workspaceServiceClient.checkPermission(projectId , permission);
        }catch (FeignException.Unauthorized e){
            log.warn("JWT TOKEN EXPIRED DURING INTERNAL MICROSERVICE CALLS");
            return false;
        }catch (FeignException e){
            log.warn("erorr checking project permission");
            return false;
        }
    }


    // this class is used to check the users and his roles before giving access to the projects

    public boolean canViewProject(Long projectId){
        return hasPermission(projectId , ProjectPermission.VIEW);
    }

    public boolean canEditProject(Long projectId){
        return hasPermission(projectId , ProjectPermission.EDIT);
    }

    public boolean canDeleteProject(Long projectId){
        return hasPermission(projectId , ProjectPermission.DELETE);
    }

}
