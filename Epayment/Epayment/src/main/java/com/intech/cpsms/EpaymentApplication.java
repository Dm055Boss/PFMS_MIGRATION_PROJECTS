package com.intech.cpsms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EpaymentApplication {

	public static void main(String[] args) {
//		  System.setProperty("org.jcp.xml.dsig.secureValidation", "false");
		SpringApplication.run(EpaymentApplication.class, args);
		System.out.println("STARTED");
	}

}
