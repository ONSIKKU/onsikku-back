package com.onsikku.onsikku_back;

import com.onsikku.onsikku_back.global.config.JpaDdlSafetyGuard;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OnsikkuBackApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(OnsikkuBackApplication.class);
		application.addInitializers(new JpaDdlSafetyGuard());
		application.run(args);
	}
}
