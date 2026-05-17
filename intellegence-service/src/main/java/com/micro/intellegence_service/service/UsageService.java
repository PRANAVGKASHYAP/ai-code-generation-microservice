package com.micro.intellegence_service.service;


import com.micro.intellegence_service.dto.PlanLimitResponse;
import com.micro.intellegence_service.dto.UsageTodayResponse;

public interface UsageService {
    UsageTodayResponse getTodayUsage(Long userId);

    PlanLimitResponse getCurrentSubscriptionLimits(Long userId);

    void recordTokenUsage(Long userId , Integer totalTokens);
    void checkDailyUsage();
}
