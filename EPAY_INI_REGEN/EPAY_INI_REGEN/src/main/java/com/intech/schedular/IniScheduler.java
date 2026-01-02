// src/main/java/com/intech/EpayIniFileGenerator/schedular/IniScheduler.java
package com.intech.schedular;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.intech.service.IniService;

@Component
@ConditionalOnProperty(prefix = "ini.scheduler", name = "enabled", havingValue = "true")
public class IniScheduler {

	private static final Logger log = LogManager.getLogger(IniScheduler.class);
	private final IniService iniService;
	private final AtomicBoolean running = new AtomicBoolean(false);

	public IniScheduler(IniService iniService) {
		this.iniService = iniService;
	}

	// fixed cron (1 min). No extra properties needed.
	@Scheduled(cron = "0 */1 * * * *")
	public void schedule() {
		if (!running.compareAndSet(false, true)) {
			log.warn("INI job skipped: previous run still in progress");
			return;
		}
		long t0 = System.currentTimeMillis();
		try {
			var res = iniService.generate();
			//log.info("INI job OK: filesWritten={} lastMsgId={} errors={}", res.generated(), res.lastMessageId(),res.errors().size());
		} catch (Exception e) {
			log.error("INI job FAILED: {}", e.getMessage(), e);
		} finally {
			running.set(false);
			log.info("INI job finished in {} ms", System.currentTimeMillis() - t0);
		}
	}
}
