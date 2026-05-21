package com.micro.workspace_service.controller;


import com.micro.workspace_service.dto.FileContentResponse;
import com.micro.workspace_service.dto.FileTreeResponse;
import com.micro.workspace_service.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/files")
public class FileController {

    private final FileService fileService;

    @GetMapping
    public ResponseEntity<FileTreeResponse> getFileTree(
            @PathVariable("projectId") Long projectId // MUST have "projectId" in the annotation
    ) {
        Long userId = 1L;
        return ResponseEntity.ok(fileService.getFileTree(projectId));
    }

    @GetMapping("/content")
    public ResponseEntity<FileContentResponse> getFileContent(
            @PathVariable("projectId") Long projectId, // MUST have "projectId"
            @RequestParam("path") String path          // MUST be @RequestParam, not @PathVariable, because UI sends ?path=
    ) {
        return ResponseEntity.ok(fileService.getFile(projectId, path));
    }
}
