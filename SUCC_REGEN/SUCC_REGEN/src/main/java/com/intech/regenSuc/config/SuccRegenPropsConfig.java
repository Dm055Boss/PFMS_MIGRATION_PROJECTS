package com.intech.regenSuc.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SuccRegenPropsConfig {
//	@Value("${success-regen-location}")
//	private String path;

	@Bean(name = "SuccRegenProps")
	public Properties loadRegenProps() throws FileNotFoundException, IOException {

		Properties p = new Properties();
		p.load(new FileInputStream("C:\\PFMS_MIG\\CPSMS\\REGENRATION\\PAYSUCC\\success-ack-regen.properties"));
		return p;

	}

}
