package com.micro.account_service.service.impl;


import com.micro.account_service.dto.Subscription.CheckoutRequest;
import com.micro.account_service.dto.Subscription.CheckoutResponse;
import com.micro.account_service.dto.Subscription.PortalResponse;
import com.micro.account_service.entity.User;
import com.micro.account_service.repository.PlanRepository;
import com.micro.account_service.repository.UserRepository;
import com.micro.account_service.service.PaymentProcessor;
import com.micro.account_service.service.SubscriptionService;
import com.micro.common_lib.enums.SubscriptionStatus;
import com.micro.common_lib.error.BadRequestException;
import com.micro.common_lib.error.ResourceNotFoundException;
import com.micro.common_lib.security.AuthUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@AllArgsConstructor
public class PaymentProcessorImpl implements PaymentProcessor {

    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private PlanRepository planRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubscriptionService subscriptionService;
    @Value("${app.frontend.url}")
    private String  successUrl;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {

        Long userId = authUtil.getCurrentUserId();
        com.micro.account_service.entity.Plan currPlan = planRepository.findById(request.planId()).orElseThrow(
                () -> new ResourceNotFoundException("The PLan with the id " + request.planId() + " Dpes not exist")
        );

        //handel the user's subscription id
        User user = userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("user not found"));


        // creating checkout session with stripe to redurect teh user to that url
        var params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder().setPrice(
                                currPlan.getStripePriceId()
                        ).setQuantity(1L).build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSubscriptionData(
                        new SessionCreateParams.SubscriptionData.Builder()
                                .setBillingMode(SessionCreateParams.SubscriptionData.BillingMode.builder()
                                        .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                                        .build())
                                .build()
                )
                .setSuccessUrl(successUrl + "/success.html?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(successUrl + "/cancel.html")
                .putMetadata("user_id" , userId.toString())
                .putMetadata("plan_id" , request.planId().toString());
        try {
            if(user.getStripeCustomerId() == null){
                params.setCustomerEmail(user.getUsername());
            }else{
                params.setCustomer(user.getStripeCustomerId());
            }
            Session session = Session.create(params.build()); // this is where the url is built
            return new CheckoutResponse(session.getUrl());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PortalResponse openCustomPortal() {
        // this is a ui page from stripe end to manage the subscription preset , like cancel , update , change payment method etc ...
        Long userId = authUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User with the id does not exist ")
        );
        String stripeCustomerId = user.getStripeCustomerId();
        if(stripeCustomerId.isEmpty()){
            throw new BadRequestException("The stripe customer id is not present for this use");
        }

        try {
            var portalSession = com.stripe.model.billingportal.Session.create(
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(stripeCustomerId)
                            .setReturnUrl(successUrl)
                            .build()
            );

            return new PortalResponse(portalSession.getUrl());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void handelWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {
        // this will get all the events sent by stripe that is gets during subscriptions
        log.debug("Getting the event of type : {}", type);
        switch (type){
            case "checkout.session.completed" -> handelCheckoutSessionCompleted((Session) stripeObject, metadata); // access to the resource should not be given here
            case "invoice.paid" -> handelInvoicePaid((Invoice) stripeObject); // this event is where the access needs to be given
            case "invoice.payment_failed" -> handelInvoicePaymentFailed((Invoice) stripeObject);
            case "customer.subscription.updated" -> handelCustomerSubscriptionUpdated((Subscription) stripeObject); // this can mean a plan change
            case "customer.subscription.deleted" -> handelCustomerSubscriptionDeleted((Subscription) stripeObject);
            default -> log.debug("Ignoring the event {}" , type);
        }
    }

    private void handelCustomerSubscriptionDeleted(Subscription subscription) {

        if(subscription == null){
            return ;
        }

        subscriptionService.cancelSubscription(subscription.getId());
    }

    private void handelCustomerSubscriptionUpdated(Subscription subscription) {
        if(subscription == null){
            log.error("The subscription object is null .....");
            return;
        }

        SubscriptionStatus status = mapStripeStatusToEnum(subscription.getStatus());

        // now handel the line  item for which the subscription is updated (in our case only one line item is preset
        SubscriptionItem lineItem = subscription.getItems().getData().get(0);

        // once the plan is updated the start time and the end time also needs to be updated
        Instant updatedStart = toInstant(lineItem.getCurrentPeriodStart());
        Instant updatedEnd = toInstant(lineItem.getCurrentPeriodEnd());
        Long planId = getPlanIdFromPrice(lineItem.getPrice()); // the Price object has stripe price id  from which plan id can be got
        // update the subscription plan with teh above data
        subscriptionService.updateSubscription(
                subscription.getId() , status , updatedStart , updatedEnd , subscription.getCancelAtPeriodEnd() , planId
        );

    }

    private Long getPlanIdFromPrice(Price price) {
        return planRepository.findByStripePriceId(price.getId())
                .map(com.micro.account_service.entity.Plan::getId)
                .orElse(null);
    }

    private Instant toInstant(Long epoch) {

        return epoch == null ? null : Instant.ofEpochSecond(epoch);
    }

    private SubscriptionStatus mapStripeStatusToEnum(String status) {
        return switch (status){
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trailing" -> SubscriptionStatus.TRAILING;
            case "past_due" -> SubscriptionStatus.PAST_DUE;
            case "cancelled" -> SubscriptionStatus.CANCELLED;
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            default -> {
                log.error("Stripe status is not matching teh ones present in the enum ");
                yield null;
            }
        };
    }

    private void handelInvoicePaymentFailed(Invoice invoice) {
        String subscriptionId = getSubscriptionIdFromInvoice(invoice);
        if(subscriptionId == null){
            return;
        }

        subscriptionService.markSubscriptionAsDue(subscriptionId);
    }

    private void handelInvoicePaid(Invoice invoice) {
        String subscriptionId = getSubscriptionIdFromInvoice(invoice);
        if(subscriptionId == null){
            return;
        }

        //get the subscription object from the subscription id
        try {
            Subscription sub = Subscription.retrieve(subscriptionId);
            var item = sub.getItems().getData().get(0); // this is the line item  , in onr case its only one item that is the plan
            Instant start = toInstant(item.getCurrentPeriodStart());
            Instant end = toInstant(item.getCurrentPeriodEnd());

            subscriptionService.renewSubscription(subscriptionId , start , end); // this renew method is called everytime teh due date of subscription is reached
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    private String getSubscriptionIdFromInvoice(Invoice invoice) {
        var parent = invoice.getParent();
        return parent.getSubscriptionDetails().getSubscription();
    }

    private void handelCheckoutSessionCompleted(Session session , Map<String, String> metadata) {

        if(session == null){
            log.error("The checkout session is null , cannot proceed ....");
            return;
        }

        //the session obj is from stripe and has all data about the subscription like plan , customer etc
        Long planId = Long.parseLong(metadata.get("plan_id"));
        Long userId = Long.parseLong(metadata.get("user_id"));

        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User with the id " + userId + "Not found")
        );

        //set this user object with subscription id that we got from stripe
        String cust_id = session.getCustomer();
        String sub_id = session.getSubscription();

        if(user.getStripeCustomerId() == null){
            user.setStripeCustomerId(cust_id); // this addition is to mark the subscription in our database
        }

        subscriptionService.activateSubscription(userId , planId , sub_id , cust_id);
    }
    
    
}
