package com.intech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EpayIniRegenApplication {

	public static void main(String[] args) {
		SpringApplication.run(EpayIniRegenApplication.class, args);
	}

}
