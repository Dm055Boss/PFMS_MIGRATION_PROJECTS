package com.intech.EXAckReader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExAckReaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExAckReaderApplication.class, args);
	}

}
