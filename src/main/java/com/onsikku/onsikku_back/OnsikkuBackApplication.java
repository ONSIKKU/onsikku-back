package com.onsikku.onsikku_back;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
public class OnsikkuBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnsikkuBackApplication.class, args);
	}
}
