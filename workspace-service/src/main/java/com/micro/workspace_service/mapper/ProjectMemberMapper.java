package com.micro.workspace_service.mapper;


import com.micro.workspace_service.dto.ProjectMemberResponse;
import com.micro.workspace_service.entity.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface ProjectMemberMapper {

    //@Mapping(target = "userId" , source = "id")
//    @Mapping(target = "Projectrole" , constant = "OWNER")
//    ProjectMemberResponse toProjectMemberResponseFromOwner(User user);

    @Mapping(target = "userId" , source = "id.userId")
    @Mapping(target = "role" , source = "role")
    ProjectMemberResponse toProjectMemberResponseFromMember(ProjectMember projectMember);
}
