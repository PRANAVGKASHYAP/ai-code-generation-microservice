package com.micro.account_service.dto.Subscription;

public record UsageTodayResponse(Integer tokensUsed , Integer tokenLimit , Integer previewsRunning , Integer previewsLimit) {
}
