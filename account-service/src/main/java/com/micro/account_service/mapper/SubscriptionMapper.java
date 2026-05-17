package com.micro.account_service.mapper;


import com.micro.account_service.dto.Subscription.PlanResponse;
import com.micro.account_service.dto.Subscription.SubscriptionResponse;
import com.micro.account_service.entity.Plan;
import com.micro.account_service.entity.Subscription;
import com.micro.common_lib.DTO.PlanDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionResponse toSubscriptionResponse(Subscription subscription);
    PlanResponse toPlanResponse(Plan plan);
    PlanDTO planToPLanDTO(Plan plan);
}
