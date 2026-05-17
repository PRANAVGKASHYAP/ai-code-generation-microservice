package com.micro.workspace_service.repository;

import com.micro.workspace_service.entity.ProcessedEvents;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventsRepository extends JpaRepository<ProcessedEvents, String> {
}
