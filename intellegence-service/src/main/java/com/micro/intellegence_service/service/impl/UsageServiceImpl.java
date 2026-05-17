package com.micro.intellegence_service.service.impl;


import com.micro.common_lib.DTO.PlanDTO;
import com.micro.common_lib.security.AuthUtil;
import com.micro.intellegence_service.client.AccountServiceClient;
import com.micro.intellegence_service.dto.PlanLimitResponse;
import com.micro.intellegence_service.dto.UsageTodayResponse;
import com.micro.intellegence_service.entity.UsageLog;
import com.micro.intellegence_service.repository.UsageLogRepository;
import com.micro.intellegence_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UsageServiceImpl implements UsageService {

    private final UsageLogRepository usageLogRepository;
    //private final SubscriptionService subscriptionService;
    private final AuthUtil authUtil;
    private final AccountServiceClient accountServiceClient;

    @Override
    public UsageTodayResponse getTodayUsage(Long userId) {
        return null;
    }

    @Override
    public PlanLimitResponse getCurrentSubscriptionLimits(Long userId) {
        return null;
    }

    @Override
    public void recordTokenUsage(Long userId, Integer totalTokens) {
        // this will store the tokens used in the  db
        LocalDate currDate = LocalDate.now();
        // update the usage logs for today by setting the tokens

        UsageLog currLog = usageLogRepository.findByUserIdAndDate(userId , currDate).orElseGet(
                () -> createLogForToday(userId , currDate)
        );

        //set teh tokens for the curr log
        currLog.setTokensUsed(
                currLog.getTokensUsed() + totalTokens
        );

        usageLogRepository.save(currLog);
    }

    @Override
    public void checkDailyUsage() {
        //this method is to check if the tokens currently being used has exceeded the total limit or not
        //SubscriptionResponse currSub = subscriptionService.getCurrentMemberSubscription();
        PlanDTO currSubPlan = accountServiceClient.getCurrentPlan();
        //PlanResponse currPlan = currSub.plan();
        if(currSubPlan.unlimitedAi()){
            return;
        }
        Long userId = authUtil.getCurrentUserId();
        LocalDate currDate = LocalDate.now();

        UsageLog currLog = usageLogRepository.findByUserIdAndDate(userId , currDate).orElseGet(
                () -> createLogForToday(userId , currDate)
        );

        if(currLog.getTokensUsed() > currSubPlan.maxTokensPerDay()){
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS , "Total limit for a day is " + currSubPlan.maxTokensPerDay() );
        }
    }


    public UsageLog createLogForToday(Long userId , LocalDate date){
        return UsageLog.builder()
                .userId(userId)
                .date(date)
                .tokensUsed(0)
                .build();
    }
}
