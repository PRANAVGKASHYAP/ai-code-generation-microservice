package com.micro.account_service.service;


import com.micro.account_service.dto.Subscription.CheckoutRequest;
import com.micro.account_service.dto.Subscription.CheckoutResponse;
import com.micro.account_service.dto.Subscription.PaymentVerification;
import com.micro.account_service.dto.Subscription.PortalResponse;
import com.stripe.model.StripeObject;

import java.util.Map;

public interface PaymentProcessor {
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request);
    public PortalResponse openCustomPortal();

    void handelWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata);

    //adding payment verification
    public PaymentVerification verifyPayment(String session_id);
}
