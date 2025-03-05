package com.g18.ecommerce.IdentifyService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class IdentifyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentifyServiceApplication.class, args);
	}

}
