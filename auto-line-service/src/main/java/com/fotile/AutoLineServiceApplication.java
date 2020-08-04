package com.fotile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class AutoLineServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoLineServiceApplication.class, args);
	}

}
