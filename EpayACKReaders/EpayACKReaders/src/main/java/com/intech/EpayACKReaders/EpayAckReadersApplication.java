package com.intech.EpayACKReaders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EpayAckReadersApplication {
    private static final Logger LOGGER = LogManager.getLogger(EpayAckReadersApplication.class);


	public static void main(String[] args) {
		LOGGER.info("EpayAckReadersApplication Started");
		SpringApplication.run(EpayAckReadersApplication.class, args);
		LOGGER.info("EpayAckReadersApplication Ended");

	}

}
