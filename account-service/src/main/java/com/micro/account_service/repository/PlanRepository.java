package com.micro.account_service.repository;

import com.micro.account_service.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    //Long getStripePriceId(Price price);

    Optional<Plan> findByStripePriceId(String id);
}
