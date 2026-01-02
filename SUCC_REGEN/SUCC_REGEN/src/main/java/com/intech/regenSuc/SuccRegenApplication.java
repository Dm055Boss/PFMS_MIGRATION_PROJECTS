package com.intech.regenSuc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SuccRegenApplication {

	public static void main(String[] args) {
		SpringApplication.run(SuccRegenApplication.class, args);
	}

}
