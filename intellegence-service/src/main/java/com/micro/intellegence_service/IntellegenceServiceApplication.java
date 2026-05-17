package com.micro.intellegence_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class IntellegenceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntellegenceServiceApplication.class, args);
	}

}
