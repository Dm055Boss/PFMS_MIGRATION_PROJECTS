package com.intech.util;

import com.intech.config.TxnProperties;
import com.intech.domain.TxnRunContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Computes run date and message id pieces using external properties.
 *
 * Date logic:
 *  txnDate = LocalDate.now(zone) + offsetDays
 */
@Component
public class DateService {

    private static final DateTimeFormatter DDMMYYYY = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final DateTimeFormatter DDMMYYYY_SLASH = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TxnProperties txnProperties;

    public DateService(TxnProperties txnProperties) {
        this.txnProperties = txnProperties;
    }

    /**
     * Builds a TxnRunContext containing date strings and message id.
     */
    public TxnRunContext buildRunContext() {
        String runId = UUID.randomUUID().toString();
        ZoneId zone = ZoneId.of(txnProperties.getDate().getZone());

        LocalDate txnDate = LocalDate.now(zone).plusDays(txnProperties.getDate().getOffsetDays());
        String ddmmyyyy = txnDate.format(DDMMYYYY);
        String ddmmyyyySlash = txnDate.format(DDMMYYYY_SLASH);

        // MessageId format as observed in sample: <BankCode>TRNREQ<ddMMyyyy><seq>
        String messageId = txnProperties.getBank().getCode() + "TRNREQ" + ddmmyyyy + txnProperties.getFileSeq();

        return new TxnRunContext(runId, txnDate, ddmmyyyy, ddmmyyyySlash, messageId);
    }

    /**
     * Converts Oracle-style dd-MON-yyyy (from to_char(...,'dd-mon-yyyy')) into dd/MM/yyyy.
     * If parsing fails, the original value is returned.
     */
    public String toDdMmYyyySlashesFromDdMonYyyy(String ddMonYyyy) {
        if (ddMonYyyy == null) {
            return "";
        }
        String v = ddMonYyyy.trim();
        if (v.isEmpty()) {
            return "";
        }
        try {
            DateTimeFormatter in = new java.time.format.DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd-MMM-yyyy")
                    .toFormatter(Locale.ENGLISH);
            DateTimeFormatter out = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(v, in).format(out);
        } catch (Exception ignored) {
            return v; // best-effort
        }
    }
}
