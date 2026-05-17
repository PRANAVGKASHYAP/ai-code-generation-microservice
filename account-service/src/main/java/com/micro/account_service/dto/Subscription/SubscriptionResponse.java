package com.micro.account_service.dto.Subscription;

import java.time.Instant;

public record SubscriptionResponse(PlanResponse plan , String status , Instant currentPeriodEnd , Long tokensUsedThisCycle) {
}
