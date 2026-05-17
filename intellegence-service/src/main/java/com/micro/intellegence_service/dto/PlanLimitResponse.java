package com.micro.intellegence_service.dto;

public record PlanLimitResponse(String planName , Integer maxTokensPerDay , Integer maxProjects , Boolean unlimitedAi) {
}
