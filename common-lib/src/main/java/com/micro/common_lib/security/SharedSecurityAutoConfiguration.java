package com.micro.common_lib.security;

import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@AutoConfiguration
public class SharedSecurityAutoConfiguration {
    //this is the class that will provide the security configuration to the microservices and also other classes and modules

    @Bean
    public AuthUtil authUtil(){
        return new AuthUtil();
    }

    @Bean
    public JwtFilter jwtFilter(AuthUtil authUtil){
        return new JwtFilter(authUtil);
    }

    // adding a bean that creates a feign client interceptor
    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if(authentication != null && authentication.getCredentials() instanceof String token){
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
}
