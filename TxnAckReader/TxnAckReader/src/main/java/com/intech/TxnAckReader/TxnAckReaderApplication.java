package com.intech.TxnAckReader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TxnAckReaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(TxnAckReaderApplication.class, args);
	}

}
