package com.micro.account_service.dto.Subscription;

//public record PlanResponse(String name , int maxProjects) {
//}

public record PlanResponse(Long id , String name , Integer maxProjects , Integer maxTokensPerDay , Boolean unlimitedAi) {
}
