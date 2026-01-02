package com.intech.schedular;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.intech.service.RejectAckService;

@Component
public class RejectAckScheduler {

    private static final Logger log = LogManager.getLogger(RejectAckScheduler.class);

    private final RejectAckService rejectAckService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public RejectAckScheduler(RejectAckService rejectAckService) {
        this.rejectAckService = rejectAckService;
    }

    /**
     * Scheduled job entry point.
     * It will skip new runs if the previous run is still active.
     */
    @Scheduled(cron = "${reject.scheduler.cron}")
    public void runRejectJob() {
        if (!running.compareAndSet(false, true)) {
            log.info("Reject ACK job is already running. Skipping this schedule.");
            return;
        }

        log.info("Starting Reject ACK job...");
        try {
            rejectAckService.generateRejectFiles();
            log.info("Reject ACK job completed.");
        } catch (Exception ex) {
            log.error("Unexpected error in Reject ACK job", ex);
        } finally {
            running.set(false);
        }
    }
}
