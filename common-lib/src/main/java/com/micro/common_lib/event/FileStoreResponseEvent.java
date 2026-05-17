package com.micro.common_lib.event;

public record FileStoreResponseEvent(
        String sagaId,
        Boolean success,
        Long projectId,
        String errorMessage
) {
}
