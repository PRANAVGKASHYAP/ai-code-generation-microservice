package com.micro.workspace_service.service.impl;


import com.micro.common_lib.error.ResourceNotFoundException;
import com.micro.workspace_service.entity.Project;
import com.micro.workspace_service.entity.ProjectFile;
import com.micro.workspace_service.repository.ProjectFileRepository;
import com.micro.workspace_service.repository.ProjectRepository;
import com.micro.workspace_service.service.ProjectTemplateService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProjectTemplateServiceImpl implements ProjectTemplateService {

    private final MinioClient minioClient;
    private final String TEMPLATE_BUCKET_NAME = "starter-projects";
    private final String templateFolder = "react-vite-tailwind-daisyui-starter";
    private final String destinationBucketName = "projects";
    private final ProjectFileRepository projectFileRepository;
    private final ProjectRepository projectRepository;

    @Override
    public void generateStarterTemplate(Long ProjectId) {
        // for the project with id projectId , copy the starter template from min io and create a new folder
        // replace this vite template with a different simple react template

        Project project = projectRepository.findById(ProjectId)
                .orElseThrow(() -> new ResourceNotFoundException("The project with id" + ProjectId + "not found"));


        Iterable<Result<Item>> templateFiles = minioClient.listObjects(
                ListObjectsArgs.builder()
                                .bucket(TEMPLATE_BUCKET_NAME)
                                .prefix(templateFolder + "/")
                                .recursive(true)
                                .build()
        );

        // the Item list has all the reference to the files stored in the bucket
        for(Result<Item> result: templateFiles){
            try {
                Item item = result.get();
                String sourceKey = item.objectName();

                String filePath = sourceKey.replaceFirst(templateFolder + "/" , "");
                String destKey = ProjectId + "/" + filePath;

                // use the minio client to copy the path
                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(destinationBucketName)
                                .object(destKey)
                                .source(
                                        CopySource.builder()
                                                .bucket(TEMPLATE_BUCKET_NAME)
                                                .object(sourceKey)
                                                .build()
                                )
                                .build()
                );

                // add all these files ot the project files also
                ProjectFile curFile = ProjectFile.builder()
                        .createdAt(Instant.now())
                        .minioObjectKey(destKey)
                        .path(filePath)
                        .project(project)
                        .updatedAt(Instant.now())
                        .build();
                projectFileRepository.save(curFile);
            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                     InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
