package com.micro.account_service.service;



import com.micro.account_service.dto.Subscription.CheckoutRequest;
import com.micro.account_service.dto.Subscription.CheckoutResponse;
import com.micro.account_service.dto.Subscription.PortalResponse;
import com.micro.account_service.dto.Subscription.SubscriptionResponse;
import com.micro.common_lib.DTO.PlanDTO;
import com.micro.common_lib.enums.SubscriptionStatus;

import java.time.Instant;

public interface SubscriptionService {
     SubscriptionResponse getCurrentMemberSubscription();

     CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request);

     PortalResponse openCustomPortal(Long userId);

    void activateSubscription(Long userId, Long planId, String subId, String custId);

    void updateSubscription(String id, SubscriptionStatus status, Instant updatedStart, Instant updatedEnd, Boolean cancelAtPeriodEnd, Long planId);

    void cancelSubscription(String id);

    void renewSubscription(String subscriptionId, Instant start, Instant end);

    void markSubscriptionAsDue(String subscriptionId);

    PlanDTO getCurrentSubscribedPlanByUser();

    //boolean canCreateNewProject();
}
