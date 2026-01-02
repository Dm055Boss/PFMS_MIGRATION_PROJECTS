package com.intech.EpayIniFileGenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan(basePackages = "com.intech.EpayIniFileGenerator")

public class EpayIniFileGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(EpayIniFileGeneratorApplication.class, args);
	}

}
