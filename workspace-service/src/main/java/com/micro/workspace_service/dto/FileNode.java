package com.micro.workspace_service.dto;

public record FileNode(String path) {

    @Override
    public String toString(){
        return path;
    }
}
