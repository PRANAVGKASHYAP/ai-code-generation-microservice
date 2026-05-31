package com.micro.intellegence_service.controller;

import com.micro.common_lib.security.AuthUtil;
import com.micro.intellegence_service.entity.UsageLog;
import com.micro.intellegence_service.repository.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1")
public class IntellegeceServiceInternalController {

    // here this method is to get the token usage and pass it to the account service , in the subscriptio controller
    private final UsageLogRepository usageLogRepository;
    private final AuthUtil authUtil;

    @GetMapping("/token-usage")
    public Long getTotalTokenUsage(){
        Long userId = authUtil.getCurrentUserId();
        List<UsageLog> totalUsage =  usageLogRepository.findByUserId(userId);
        AtomicReference<Long> total = new AtomicReference<>(0L);
        totalUsage.stream()
                .forEach(ele -> {
                    total.updateAndGet(v -> v + ele.getTokensUsed());
                });
        return total.get();
    }
}
