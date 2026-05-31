package com.micro.intellegence_service.repository;

import com.micro.intellegence_service.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UsageLogRepository extends JpaRepository<UsageLog , Long> {
    Optional<UsageLog> findByUserIdAndDate(Long userId, LocalDate currDate);

    List<UsageLog> findByUserId(Long userId);
}
