package com.micro.workspace_service.mapper;


import com.micro.workspace_service.dto.FileNode;
import com.micro.workspace_service.entity.ProjectFile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectFileMapper {

    List<FileNode> toListOfFileode(List<ProjectFile> projectFileList);
}
