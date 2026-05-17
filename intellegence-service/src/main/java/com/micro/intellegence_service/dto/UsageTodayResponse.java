package com.micro.intellegence_service.dto;

public record UsageTodayResponse(Integer tokensUsed , Integer tokenLimit , Integer previewsRunning , Integer previewsLimit) {
}
