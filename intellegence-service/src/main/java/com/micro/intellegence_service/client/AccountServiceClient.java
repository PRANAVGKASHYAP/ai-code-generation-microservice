package com.micro.intellegence_service.client;

import com.micro.common_lib.DTO.PlanDTO;
import com.micro.common_lib.DTO.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@FeignClient(name = "account-service" , path = "/account" , url = "${ACCOUNT_SERVICE_URI:}")
public interface AccountServiceClient{
    //creating method definitions that match the internal controller of the account service

    // on calling these methods the token needs to be passed in the headers as the account service used auth util to decode the token and get the user id
    @GetMapping("internal/v1/users/mail-id")
    public Optional<UserDTO> findByMailId(@RequestParam String mail);

    @GetMapping("internal/v1/billing/current-plan")
    public PlanDTO getCurrentPlan();

    @GetMapping("internal/v1//user/{id}")
    public UserDTO getUserById(@PathVariable Long id);
}
