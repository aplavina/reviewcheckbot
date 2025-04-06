package com.aplavina.reviewcheckbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.aplavina.reviewcheckbot.client")
public class ReviewcheckbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviewcheckbotApplication.class, args);
	}

}
