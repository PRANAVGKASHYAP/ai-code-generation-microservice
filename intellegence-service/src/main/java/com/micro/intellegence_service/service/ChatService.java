package com.micro.intellegence_service.service;


import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;

public interface ChatService {

    public List<ChatResponse> getProjectChatHistory(Long projectId);
}
