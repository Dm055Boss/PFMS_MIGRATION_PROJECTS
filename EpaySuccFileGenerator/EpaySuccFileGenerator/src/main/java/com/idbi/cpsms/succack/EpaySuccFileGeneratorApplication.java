package com.idbi.cpsms.succack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EpaySuccFileGeneratorApplication {

	public static void main(String[] args) {
		System.out.println("started Success file creation");
		SpringApplication.run(EpaySuccFileGeneratorApplication.class, args);
	}

}
