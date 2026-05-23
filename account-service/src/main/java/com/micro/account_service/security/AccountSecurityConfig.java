package com.micro.account_service.security;

import com.micro.common_lib.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class AccountSecurityConfig {

    @Autowired
    private JwtFilter filter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // http://localhost:8080/api/v1/account/payments/checkout
        httpSecurity
                .csrf(config -> config.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**" , "/webhooks/**" , "/actuator/**" , "/internal/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(filter , UsernamePasswordAuthenticationFilter.class);
        //.formLogin(ele -> ele.disable());
        return httpSecurity.build();
    }


}
