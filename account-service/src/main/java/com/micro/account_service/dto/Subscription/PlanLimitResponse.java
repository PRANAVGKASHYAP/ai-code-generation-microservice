package com.micro.account_service.dto.Subscription;

public record PlanLimitResponse(String planName , Integer maxTokensPerDay , Integer maxProjects , Boolean unlimitedAi) {
}
