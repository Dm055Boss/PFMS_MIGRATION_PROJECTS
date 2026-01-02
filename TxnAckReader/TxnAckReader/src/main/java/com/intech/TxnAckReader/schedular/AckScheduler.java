package com.intech.TxnAckReader.schedular;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.intech.TxnAckReader.service.AckProcessorService;

@Component
public class AckScheduler {

    private static final Logger log = LoggerFactory.getLogger(AckScheduler.class);

    private final AckProcessorService processorService;

    public AckScheduler(AckProcessorService processorService) {
        this.processorService = processorService;
    }

    @Scheduled(cron = "${scheduler.ack.cron}")
    public void run() {
        log.info("ACK job triggered.");
        processorService.processAckFolder();
    }
}
