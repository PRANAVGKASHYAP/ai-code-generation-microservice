package com.micro.workspace_service.service.impl;


import com.micro.common_lib.error.ResourceNotFoundException;
import com.micro.workspace_service.dto.FileContentResponse;
import com.micro.workspace_service.dto.FileNode;
import com.micro.workspace_service.dto.FileTreeResponse;
import com.micro.workspace_service.entity.Project;
import com.micro.workspace_service.entity.ProjectFile;
import com.micro.workspace_service.mapper.ProjectFileMapper;
import com.micro.workspace_service.repository.ProjectFileRepository;
import com.micro.workspace_service.repository.ProjectRepository;
import com.micro.workspace_service.service.FileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final MinioClient minioClient;
    private final ProjectFileMapper projectFileMapper;

    @Value("${minio.project-bucket}")
    private  String bucketName;

    @Override
    public FileTreeResponse getFileTree(Long projectId) {

        List<ProjectFile> projectFiles = projectFileRepository.findByProjectId(projectId);

        List<FileNode> nodes =  projectFileMapper.toListOfFileode(projectFiles);
        return new FileTreeResponse(nodes);
    }

    @Override
    public FileContentResponse getFile(Long projectId, String path) {

        String object = projectId + "/" + path;
        try {
            InputStream inp = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket("projects")
                            .object(object)
                            .build()
            );

            String content = new String(inp.readAllBytes() , StandardCharsets.UTF_8);
            return new FileContentResponse(path , content);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveFile(Long projectId, String filePath, String fileContent) {
        Project project = projectRepository.findById(projectId)
                        .orElseThrow( () -> new ResourceNotFoundException("The project with the id " + projectId + "Not found"));

        log.info("saving the file in postgrese and also in minIO");

        // save the metadata in postgres ,
        // store the actual file in minio

        String cleanPath = filePath.startsWith("/")? filePath.substring(1) : filePath;
        String objectKey = projectId + "/" + cleanPath;

        try {
            //convert string to byte stream
            byte[] contentInBytes = fileContent.getBytes(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(contentInBytes);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .stream(inputStream , contentInBytes.length, -1)
                            .object(objectKey)
                            .contentType(getContentType(cleanPath))
                            .build()
            );
            
            // check if the file already exists in the db , if not create it else update it and in both cases update the timestamp and save it
            ProjectFile file = projectFileRepository.findByProjectIdAndPath(projectId , cleanPath)
                    .orElseGet(
                            () -> ProjectFile.builder()
                                    .project(project)
                                    .path(cleanPath)
                                    .minioObjectKey(objectKey)
                                    .createdAt(Instant.now())
                                    .build()
                    );
            file.setUpdatedAt(Instant.now());
            projectFileRepository.save(file); // this is just to save the metadata of the file and not the actual content of the file

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private String getContentType(String cleanPath) {
        String type = URLConnection.guessContentTypeFromName(cleanPath);
        if(type != null ) return type;

        else {
            if (  cleanPath.endsWith(".jsx") || cleanPath.endsWith(".ts") ||  cleanPath.endsWith(".tsx")   ) return "text/javascript";
            if (  cleanPath.endsWith(".json") ) return "text/json";
            if (  cleanPath.endsWith(".css")   ) return "text/css";
        }
        return "text/plain";
    }
}
