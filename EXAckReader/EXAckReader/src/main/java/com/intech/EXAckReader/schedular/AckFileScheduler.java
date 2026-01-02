package com.intech.EXAckReader.schedular;

import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.intech.EXAckReader.service.AckFileProcessor;

/**
 * Scheduler that runs based on cron from external properties.
 */
@Component
public class AckFileScheduler {
	private static final Logger log = LoggerFactory.getLogger(AckFileScheduler.class);

	private final AckFileProcessor processor;
	private final ReentrantLock runLock = new ReentrantLock();

	@Value("${app.scheduler.enabled:true}")
	private boolean enabled;

	@Value("${app.ack.input.dir}")
	private String ackInputDir;

	public AckFileScheduler(AckFileProcessor processor) {
		this.processor = processor;
	}

	@Scheduled(cron = "${app.scheduler.cron}")
	public void run() {
		if (!enabled) {
			return;
		}

		// Prevent overlapping executions
		if (!runLock.tryLock()) {
			log.info("Previous run still executing, skipping this schedule tick.");
			return;
		}

		try {
			processor.processDirectoryOnce(Path.of(ackInputDir));
		} finally {
			runLock.unlock();
		}
	}
}
