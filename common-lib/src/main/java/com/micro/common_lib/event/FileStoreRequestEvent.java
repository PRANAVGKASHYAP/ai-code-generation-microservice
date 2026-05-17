package com.micro.common_lib.event;

public record FileStoreRequestEvent (
        Long projectId,
        String sagaId,
        String filePath,
        String content
        //Long userId
){
    // this is the event that will be sent by the intellegence service to store the file in minio
    // the workspace service will listen to this event and store the file in minio
}
