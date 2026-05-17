package com.micro.account_service.repository;

import com.micro.account_service.entity.Subscription;
import com.micro.common_lib.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface SubscriptionRepository extends JpaRepository<Subscription , Long> {
    Optional<Subscription> findByUserIdAndStatusIn(Long userId , Set<SubscriptionStatus> active);

    boolean existsByStripeSubscriptionId(String subId);

    Optional<Subscription> findByStripeSubscriptionId(String subscriptionId);
}
