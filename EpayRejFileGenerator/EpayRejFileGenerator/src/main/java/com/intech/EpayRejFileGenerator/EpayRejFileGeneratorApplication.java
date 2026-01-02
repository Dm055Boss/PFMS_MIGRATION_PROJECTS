package com.intech.EpayRejFileGenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EpayRejFileGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(EpayRejFileGeneratorApplication.class, args);
	}

}
