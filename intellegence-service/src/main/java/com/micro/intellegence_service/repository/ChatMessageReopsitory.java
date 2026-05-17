package com.micro.intellegence_service.repository;


import com.micro.intellegence_service.entity.ChatMessage;
import com.micro.intellegence_service.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageReopsitory extends JpaRepository<ChatMessage, Long> {

    @Query("""
            select distinct m from ChatMessage m
            left join fetch m.events e
            where m.chatSession = :chatSession
            order by m.createdAt ASC , e.sequenceOrder ASC
            """)
    List<ChatMessage> findByChatSessoin(ChatSession chatSession);
}
