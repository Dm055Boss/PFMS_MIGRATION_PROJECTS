package com.idbi.cpsms.succack.config;

import java.io.FileInputStream;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SuccessAckPropertiesConfig {

    @Bean(name = "successAckProperties")
    public Properties successAckProperties() throws Exception {
        Properties p = new Properties();
        p.load(new FileInputStream("C:\\PFMS_MIG\\CPSMS\\PAYSUCC\\success-ack-job.properties"));
        return p;
    }
}
