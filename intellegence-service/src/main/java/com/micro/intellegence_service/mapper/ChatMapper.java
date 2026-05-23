package com.micro.intellegence_service.mapper;



import com.micro.intellegence_service.dto.ChatResponse;
import com.micro.intellegence_service.entity.ChatMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    List<ChatResponse> fromMessageToChatRespnse(List<ChatMessage> messages);
}
