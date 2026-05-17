package com.micro.intellegence_service.service.impl;


import com.micro.common_lib.security.AuthUtil;
import com.micro.intellegence_service.entity.ChatMessage;
import com.micro.intellegence_service.entity.ChatSession;
import com.micro.intellegence_service.entity.ChatSessionId;
import com.micro.intellegence_service.mapper.ChatMapper;
import com.micro.intellegence_service.repository.ChatMessageReopsitory;
import com.micro.intellegence_service.repository.ChatSessionRepository;
import com.micro.intellegence_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageReopsitory chatMessageReopsitory;
    private final AuthUtil authUtil;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMapper chatMapper;
    @Override
    public List<ChatResponse> getProjectChatHistory(Long projectId) {

        // create a chat session with the user id and the project id , then find in the shat messages with this chat session
        ChatSessionId chatSessionId = new ChatSessionId(authUtil.getCurrentUserId(),  projectId);
        ChatSession currChatSession = chatSessionRepository.getReferenceById(chatSessionId);

        List<ChatMessage> result = chatMessageReopsitory.findByChatSessoin(currChatSession);

        // use mapper to convert chat message to chat response
        return chatMapper.fromMessageToChatRespnse(result);
    }
}
