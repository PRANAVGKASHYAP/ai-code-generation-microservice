package com.micro.account_service.controller;

import com.micro.account_service.mapper.UserMapper;
import com.micro.account_service.repository.UserRepository;
import com.micro.account_service.service.SubscriptionService;
import com.micro.common_lib.DTO.PlanDTO;
import com.micro.common_lib.DTO.UserDTO;
import com.micro.common_lib.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1")
public class InternalAccountController {
    // this controller will be used by other microservices to talk to the account ervice to execute required methods

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SubscriptionService subscriptionService;


    @GetMapping("/user/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userRepository.findById(id).map(userMapper::UserToUserDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id" + id));
    }

    @GetMapping("users/mail-id")
    public UserDTO findByMailId(@RequestParam String mail){
        return userRepository.findByUsernameIgnoreCase(mail).map(userMapper::UserToUserDTO)
                .orElseThrow(() -> new RuntimeException("User not found with mail id " + mail));
    }

    @GetMapping("/billing/current-plan")
    public PlanDTO getCurrentPlan() {
        return subscriptionService.getCurrentSubscribedPlanByUser();
    }
}
