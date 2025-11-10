package com.onsikku.onsikku_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class OnsikkuBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnsikkuBackApplication.class, args);
	}

}
