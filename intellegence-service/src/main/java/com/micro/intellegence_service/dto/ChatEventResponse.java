package com.micro.intellegence_service.dto;


import com.micro.common_lib.enums.ChatEventType;

public record ChatEventResponse(

        Long id,
        ChatEventType chatEventType,
        Integer sequenceOrder,
        String content,
        String filePath,
        String metadata
) {
}
