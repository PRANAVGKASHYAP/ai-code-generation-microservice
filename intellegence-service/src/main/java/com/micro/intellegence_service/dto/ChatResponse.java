package com.micro.intellegence_service.dto;


import com.micro.common_lib.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(

        Long id,
        //ChatSession chatSession,
        List<ChatEventResponse>events,
        String content,
        Integer tokensUsed,
        Instant createdAt,
        MessageRole messageRole
) {
}
