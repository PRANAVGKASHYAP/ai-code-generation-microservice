package com.micro.account_service.service.impl;


import com.micro.account_service.dto.Subscription.CheckoutRequest;
import com.micro.account_service.dto.Subscription.CheckoutResponse;
import com.micro.account_service.dto.Subscription.PortalResponse;
import com.micro.account_service.dto.Subscription.SubscriptionResponse;
import com.micro.account_service.entity.Plan;
import com.micro.account_service.entity.Subscription;
import com.micro.account_service.entity.User;
import com.micro.account_service.mapper.SubscriptionMapper;
import com.micro.account_service.repository.PlanRepository;
import com.micro.account_service.repository.SubscriptionRepository;
import com.micro.account_service.repository.UserRepository;
import com.micro.account_service.service.SubscriptionService;
import com.micro.common_lib.DTO.PlanDTO;
import com.micro.common_lib.enums.SubscriptionStatus;
import com.micro.common_lib.error.ResourceNotFoundException;
import com.micro.common_lib.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final AuthUtil authUtil;
    private final SubscriptionRepository subRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final SubscriptionMapper mapper;
    //private final ProjectMemberRepository projectMemberRepository;

    @Override
    public SubscriptionResponse getCurrentMemberSubscription() {
        Long userId = authUtil.getCurrentUserId();
        Subscription currentSub =  subRepository.findByUserIdAndStatusIn(userId , Set.of(
                SubscriptionStatus.ACTIVE , SubscriptionStatus.PAST_DUE , SubscriptionStatus.TRAILING
        )).orElse( new Subscription());

        return mapper.toSubscriptionResponse(currentSub);
    }

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {
        // on clicking checkout u need to give a link that will open the stripe ui for payments

        //1. in the checkout obj , the plan id is given
        //2. using the plan make a url
        //3. return this url

        return null;
    }

    @Override
    public PortalResponse openCustomPortal(Long userId) {

        return null;
    }

    @Override
    public void activateSubscription(Long userId, Long planId, String subId, String custId) {
        // this is the method to mark the subscription as active and this method is called from the checkout page

        // this method is called after the checkout page closes , ther is no gaurentee that the incoive payment failed or succeeded

        boolean subscriptionAldreadyExists = subRepository.existsByStripeSubscriptionId(subId);
        if (subscriptionAldreadyExists) return;

        // create a new subscription object and store it in ure database
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("user with the id" + userId + "not present")
        );
        Plan plan = planRepository.findById(planId).orElseThrow(
                () -> new ResourceNotFoundException("plan with the id" + planId + "not present")
        );

        Subscription subscription = Subscription.builder()
                .plan(plan)
                .user(user)
                .stripeSubscriptionId(subId)
                .status(SubscriptionStatus.INCOMPLETE) // this will be marked complete by invoice paid event
                .build();

        subRepository.save(subscription);
    }

    @Override
    public void updateSubscription(String id, SubscriptionStatus status, Instant updatedStart, Instant updatedEnd, Boolean cancelAtPeriodEnd, Long planId) {
        //this is for plan switch or any subscription change

        Subscription subscription = subRepository.findByStripeSubscriptionId(id).orElseThrow(
                () -> new ResourceNotFoundException("Subscription not found")
        );

        if(status != null && status != subscription.getStatus()){
            subscription.setStatus(status);
        }
        if(updatedStart != subscription.getCurrentPeriodStart()){
            subscription.setCurrentPeriodStart(updatedStart);
        }
        if(updatedEnd != subscription.getCurrentPeriodEnd()){
            subscription.setCurrentPeriodEnd(updatedEnd);
        }
        if(!Objects.equals(planId, subscription.getPlan().getId())){
            Plan newPlan = planRepository.findById(planId).orElseThrow(() -> new ResourceNotFoundException("No plan with this id "));
            subscription.setPlan(newPlan);
        }
        if(cancelAtPeriodEnd!=null && cancelAtPeriodEnd != subscription.getCancelAtPeriodEnd()){
            subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
        }

        subRepository.save(subscription);
    }

    @Override
    public void cancelSubscription(String id) {
        Subscription sub = subRepository.findByStripeSubscriptionId(id).orElseThrow(
                () -> new ResourceNotFoundException("The subscription with this id not found" + id)
        );

        sub.setStatus(SubscriptionStatus.CANCELLED);
        subRepository.save(sub);
    }

    @Override
    public void renewSubscription(String subscriptionId, Instant start, Instant end) {
        // this is the method called on successfull invoice payment
        Subscription sub = subRepository.findByStripeSubscriptionId(subscriptionId).orElseThrow(
                () -> new ResourceNotFoundException("subscription not found with teh given id " + subscriptionId)
        );

        Instant newStart = start != null ? start : sub.getCurrentPeriodEnd();

        sub.setCurrentPeriodStart(newStart);
        sub.setCurrentPeriodEnd(end);
        if(sub.getStatus() == SubscriptionStatus.INCOMPLETE || sub.getStatus() == SubscriptionStatus.PAST_DUE){
            sub.setStatus(SubscriptionStatus.ACTIVE);
        }
        subRepository.save(sub);
    }

    @Override
    public void markSubscriptionAsDue(String subscriptionId) {
        // this is when the invoice failed
        Subscription sub = subRepository.findByStripeSubscriptionId(subscriptionId).orElseThrow(
                () -> new ResourceNotFoundException("The subscription with this id not found" + subscriptionId)
        );

        if(sub.getStatus() == SubscriptionStatus.PAST_DUE){
            log.warn("This subscription is aldready PAST DUE and still not paid for ");
        }

        else{
            sub.setStatus(SubscriptionStatus.PAST_DUE);
        }

        subRepository.save(sub);
        // can mail the users to remind them to py up for the subscription
    }

    @Override
    public PlanDTO getCurrentSubscribedPlanByUser() {
        Long userId = authUtil.getCurrentUserId();
        Subscription currentSub =  subRepository.findByUserIdAndStatusIn(userId , Set.of(
                SubscriptionStatus.ACTIVE , SubscriptionStatus.PAST_DUE , SubscriptionStatus.TRAILING
        )).orElse( new Subscription());

        Plan currPlan = currentSub.getPlan();
        return mapper.planToPLanDTO(currPlan);
    }

    // checking if a new project can be created or not will be in the workspace service
//    @Override
//    public boolean canCreateNewProject() {
//        // check the plan of the user and see how many projects are left that can be created
//        SubscriptionResponse currSub = getCurrentMemberSubscription();
//        int projectsOwned = projectMemberRepository.countProjectsOwnedByUser(authUtil.getCurrentUserId());
//        if(currSub.plan() == null){
//            //there is no plan the user has subscribed to , so its in free tier
//            return projectsOwned < 1;
//        }
//        return currSub.plan().maxProjects() > projectsOwned;
//    }
}
