package com.intech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TxnReportingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TxnReportingApplication.class, args);
	}

}
