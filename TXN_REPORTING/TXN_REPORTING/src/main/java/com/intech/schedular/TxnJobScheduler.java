package com.intech.schedular;

import com.intech.config.SchedulerProperties;
import com.intech.config.TxnProperties;
import com.intech.service.TxnJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler trigger for the TXN job.
 *
 * - Cron is externalized.
 * - Zone is externalized (Asia/Kolkata by default).
 * - Exceptions are caught and logged so the scheduler thread keeps running.
 */
@Component
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class TxnJobScheduler {

    private static final Logger log = LoggerFactory.getLogger(TxnJobScheduler.class);

    private final TxnJobService txnJobService;
    private final SchedulerProperties schedulerProperties;
    private final TxnProperties txnProperties;

    public TxnJobScheduler(TxnJobService txnJobService,
                           SchedulerProperties schedulerProperties,
                           TxnProperties txnProperties) {
        this.txnJobService = txnJobService;
        this.schedulerProperties = schedulerProperties;
        this.txnProperties = txnProperties;
    }

    @Scheduled(cron = "${scheduler.cron}", zone = "${txn.date.zone:Asia/Kolkata}")
    public void run() {
        try {
            txnJobService.runJob();
        } catch (Exception e) {
            log.error("JOB_RUN_FAILED cron='{}' zone='{}': {}",
                    schedulerProperties.getCron(), txnProperties.getDate().getZone(), e.getMessage(), e);
        }
    }
}
