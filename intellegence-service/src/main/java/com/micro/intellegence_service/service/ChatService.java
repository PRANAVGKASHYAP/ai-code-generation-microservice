package com.micro.intellegence_service.service;



import com.micro.intellegence_service.dto.ChatResponse;

import java.util.List;

public interface ChatService {

    public List<ChatResponse> getProjectChatHistory(Long projectId);
}
