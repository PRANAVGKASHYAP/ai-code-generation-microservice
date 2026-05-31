package com.micro.account_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "intellegence-service" , path = "/intellegence" , url = "${INTELLEGENCE_SERVICE_URI:}")
public interface IntellegenceServiceClient {

    @GetMapping("internal/v1/token-usage")
    public Long getTotalTokenUsage();
}
