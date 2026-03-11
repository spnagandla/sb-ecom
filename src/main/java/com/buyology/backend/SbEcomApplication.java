package com.buyology.backend;

import com.buyology.backend.config.InfisicalPropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SbEcomApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SbEcomApplication.class);
		app.addInitializers(new InfisicalPropertySource());
		app.run(args);
	}

}
