package com.micro.account_service.controller;


import com.micro.account_service.dto.Subscription.*;
import com.micro.account_service.service.PaymentProcessor;
import com.micro.account_service.service.SubscriptionService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BillingController {

    //private final PlanService planService;
    private final SubscriptionService subscriptionService;
    private final PaymentProcessor paymentProcessor;
    @Value("${stripe.webhook.secret}")
    private  String webhookSecret;

    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>>getAllPlans(){
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/me/subscription")
    public ResponseEntity<SubscriptionResponse>getMySubscription(){
        return ResponseEntity.ok(subscriptionService.getCurrentMemberSubscription());
    }

    @PostMapping("/payments/checkout")
    public ResponseEntity<CheckoutResponse>createCheckout(@RequestBody CheckoutRequest request){
        return ResponseEntity.ok(paymentProcessor.createCheckoutSessionUrl(request));
    }

    @PostMapping("/payments/portal")
    public ResponseEntity<PortalResponse>openPortal(){
        return ResponseEntity.ok(paymentProcessor.openCustomPortal());
    }

    @PostMapping("/webhooks/payment")
    public ResponseEntity<String> handlePaymentWebhooks(
            @RequestBody String payload , @RequestHeader("Stripe-Signature") String signature

    ){
        System.out.println("Secret length: " + webhookSecret.length());
        System.out.println("Does it end with \\r? " + webhookSecret.endsWith("\r"));
        System.out.println("Does it end with \\n? " + webhookSecret.endsWith("\n"));
        try {
            Event event = Webhook.constructEvent(payload , signature , webhookSecret);

            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = null;

            if(deserializer.getObject().isPresent()){
                stripeObject = deserializer.getObject().get();
            }else{
                try {
                    stripeObject = deserializer.deserializeUnsafe();
                    if(stripeObject == null){
                        return ResponseEntity.ok().build();
                    }
                }catch (Exception e){
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Deserialization faild");
                }
            }

            // process the de serialized event
            Map<String , String >metadata = new HashMap<>();
            if(stripeObject instanceof Session session){
                metadata = ((Session) stripeObject).getMetadata();
            }

            paymentProcessor.handelWebhookEvent(event.getType() , stripeObject , metadata);
            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {
            throw new RuntimeException(e);
        }

    }

    @GetMapping("/payments/verify")
    public ResponseEntity<PaymentVerification> verifyUserPayment(@RequestParam("session_id") String sessionId) {
        return ResponseEntity.ok(paymentProcessor.verifyPayment(sessionId));
    }

    // adding a saperate get mapping to find the total tokens used
    @GetMapping("/me/tokens")
    public ResponseEntity<Long> getTotalTokensUsed() {
        return ResponseEntity.ok(subscriptionService.getTotalTokenUsage());
    }

}
