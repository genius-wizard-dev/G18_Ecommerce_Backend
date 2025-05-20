package com.vutran0943.basket_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BasketServiceApplication {

	public static void main(String[] args) {
		System.out.print(">>>>>>>>> ok222");
		SpringApplication.run(BasketServiceApplication.class, args);
	}

}
