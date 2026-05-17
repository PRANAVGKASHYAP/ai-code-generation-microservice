package com.micro.workspace_service.service;


import com.micro.workspace_service.dto.FileContentResponse;
import com.micro.workspace_service.dto.FileTreeResponse;

public interface FileService {
    FileTreeResponse getFileTree(Long projectId);

    FileContentResponse getFile(Long projectId, String path);

    void saveFile(Long projectId, String filePath, String fileContent);
}
