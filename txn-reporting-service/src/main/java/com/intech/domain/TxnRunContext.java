package com.intech.domain;

import java.time.LocalDate;

/**
 * Computed context for a single job run (date strings, messageId, etc.).
 */
public class TxnRunContext {

    private final String runId;
    private final LocalDate txnDate;
    private final String txnDateDdMmYyyy;
    private final String txnDateDdMmYyyySlashes;
    private final String messageId;

    public TxnRunContext(String runId,
                         LocalDate txnDate,
                         String txnDateDdMmYyyy,
                         String txnDateDdMmYyyySlashes,
                         String messageId) {
        this.runId = runId;
        this.txnDate = txnDate;
        this.txnDateDdMmYyyy = txnDateDdMmYyyy;
        this.txnDateDdMmYyyySlashes = txnDateDdMmYyyySlashes;
        this.messageId = messageId;
    }

    public String getRunId() { return runId; }

    public LocalDate getTxnDate() { return txnDate; }

    public String getTxnDateDdMmYyyy() { return txnDateDdMmYyyy; }

    public String getTxnDateDdMmYyyySlashes() { return txnDateDdMmYyyySlashes; }

    public String getMessageId() { return messageId; }
}
