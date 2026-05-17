package com.micro.intellegence_service.repository;

import com.micro.intellegence_service.entity.ChatSession;
import com.micro.intellegence_service.entity.ChatSessionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession , ChatSessionId> {
}
