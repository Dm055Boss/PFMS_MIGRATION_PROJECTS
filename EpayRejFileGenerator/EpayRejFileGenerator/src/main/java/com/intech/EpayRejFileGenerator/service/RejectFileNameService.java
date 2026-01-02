package com.intech.EpayRejFileGenerator.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.intech.EpayRejFileGenerator.config.RejectAckProperties;
import com.intech.EpayRejFileGenerator.exception.RejectAckException;

/**
 * Handles generation of MessageId / filename based on counter file PAYINICNTR.properties.
 *
 * File sample (legacy):
 *   DATE=17112025
 *   PAYSUC=6   (used by Success Ack generator)
 *
 * For Reject ACK we will use key PAYREJ (configurable via reject.counter-key).
 */
@Service
public class RejectFileNameService {

    private static final Logger log = LogManager.getLogger(RejectFileNameService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final RejectAckProperties props;

    // For optional rollback
    private Integer lastCounterValue = null;
    private String lastDate = null;

    public RejectFileNameService(RejectAckProperties props) {
        this.props = props;
    }

    /**
     * Generates the MessageId for a reject file and persists the updated counter.
     * Pattern: {bankCode}{paymentProduct}REJPAY{ddMMyyyy}{serial}
     *
     * Example: 691EPAREJPAY17112025700001
     */
    public synchronized String generateMessageId(String paymentProduct, String bankCode) {
        String counterPath = props.getCounterFile();
        String counterKey = props.getCounterKey();
        int counterStart = props.getCounterStart();

        if (counterPath == null || counterPath.isBlank()) {
            throw new RejectAckException("reject.counter-file is not configured");
        }
        if (counterKey == null || counterKey.isBlank()) {
            throw new RejectAckException("reject.counter-key is not configured");
        }

        String today = LocalDate.now().format(DATE_FMT);
        File file = new File(counterPath);
        log.debug("Using counter file [{}]", file.getAbsolutePath());

        Properties p = new Properties();

        try {
            // 1) Load existing counter file if present
            if (file.exists()) {
                try (FileInputStream in = new FileInputStream(file)) {
                    p.load(in);
                }
            } else {
                log.warn("Counter file [{}] does not exist. It will be created.", file.getAbsolutePath());
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    boolean created = parent.mkdirs();
                    if (!created) {
                        log.warn("Could not create parent directory [{}] for counter file.", parent.getAbsolutePath());
                    }
                }
                // file will be created when we open FileOutputStream below
            }

            String currentDate = p.getProperty("DATE");
            if (currentDate == null || currentDate.trim().isEmpty()) {
                currentDate = today;
            }

            int serial;
            if (currentDate.equals(today)) {
                // Same date – increment existing counter
                String existing = p.getProperty(counterKey);
                if (existing == null || existing.trim().isEmpty()) {
                    serial = counterStart;
                } else {
                    try {
                        serial = Integer.parseInt(existing) + 1;
                    } catch (NumberFormatException e) {
                        log.warn("Invalid counter value '{}' for key {}. Resetting to start {}.",
                                existing, counterKey, counterStart, e);
                        serial = counterStart;
                    }
                }
            } else {
                // New date – reset counter
                currentDate = today;
                serial = counterStart;
            }

            String srno = String.valueOf(serial);

            // Persist DATE and our counter key – do not touch other keys (like PAYSUC)
            p.setProperty("DATE", currentDate);
            p.setProperty(counterKey, srno);

            try (FileOutputStream out = new FileOutputStream(file)) {
                p.store(out, "Updated by Reject Ack generator");
            }

            lastCounterValue = serial;
            lastDate = currentDate;

            String messageId = bankCode + paymentProduct + "REJPAY" + today + srno;
            log.info("Generated Reject MessageId={} using DATE={} and {}={}", messageId, currentDate, counterKey, srno);
            return messageId;

        } catch (IOException ex) {
            log.error("Error while generating MessageId using counter file {}", file.getAbsolutePath(), ex);
            throw new RejectAckException("Failed to generate reject MessageId", ex);
        }
    }

    /**
     * Optional rollback if we want to decrement the counter when batch processing fails.
     * Similar to legacy decreseFileCounter().
     */
    public synchronized void rollbackLastIncrement() {
        if (lastCounterValue == null || lastDate == null) {
            // Nothing to rollback
            return;
        }

        String counterPath = props.getCounterFile();
        String counterKey = props.getCounterKey();

        if (counterPath == null || counterPath.isBlank() || counterKey == null || counterKey.isBlank()) {
            return;
        }

        File file = new File(counterPath);
        if (!file.exists()) {
            return;
        }

        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            p.load(in);
        } catch (IOException e) {
            log.warn("Could not load counter file for rollback: {}", file.getAbsolutePath(), e);
            return;
        }

        String currentDate = p.getProperty("DATE");
        if (!lastDate.equals(currentDate)) {
            // Date changed, don't rollback
            return;
        }

        int decremented = lastCounterValue - 1;
        if (decremented < props.getCounterStart()) {
            decremented = props.getCounterStart();
        }

        p.setProperty("DATE", currentDate);
        p.setProperty(counterKey, String.valueOf(decremented));

        try (FileOutputStream out = new FileOutputStream(file)) {
            p.store(out, "Rollback by Reject Ack generator");
            log.info("Rolled back counter {} to {} for DATE={}", counterKey, decremented, currentDate);
        } catch (IOException ex) {
            log.warn("Failed to rollback counter file {}", file.getAbsolutePath(), ex);
        }
    }
}
