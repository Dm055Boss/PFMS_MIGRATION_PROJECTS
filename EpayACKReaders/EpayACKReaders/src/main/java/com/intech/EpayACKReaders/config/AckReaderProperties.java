// src/main/java/com/intech/epayackreader/config/AckReaderProperties.java
package com.intech.EpayACKReaders.config;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "epay")
public class AckReaderProperties {

	private static final Logger LOGGER = LogManager.getLogger(AckReaderProperties.class);

	private boolean schedulerEnabled = true;
	
	@Value("${epay.ack.cron}")
	private String cron;
	
	private int maxFilesPerRun = 50;


	@Value("${epay.ack.input-dirs}")
	private List<String> inputDirs;

	
	private String fileGlob = "*.xml";

	// getters & setters...

	public boolean isSchedulerEnabled() {
		return schedulerEnabled;
	}

	public void setSchedulerEnabled(boolean schedulerEnabled) {
		this.schedulerEnabled = schedulerEnabled;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public int getMaxFilesPerRun() {
		return maxFilesPerRun;
	}

	public void setMaxFilesPerRun(int maxFilesPerRun) {
		this.maxFilesPerRun = maxFilesPerRun;
	}


	public List<String> getInputDirs() {
		return inputDirs;
	}

	public void setInputDirs(List<String> inputDirs) {
		this.inputDirs = inputDirs;
	}

	public String getFileGlob() {
		return fileGlob;
	}

	public void setFileGlob(String fileGlob) {
		this.fileGlob = fileGlob;
	}
}
