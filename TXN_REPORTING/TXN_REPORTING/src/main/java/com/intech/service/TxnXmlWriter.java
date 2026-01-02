package com.intech.service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.intech.config.TxnProperties;
import com.intech.domain.AccountReport;
import com.intech.domain.BalanceInfo;
import com.intech.domain.CounterpartyInfo;
import com.intech.domain.TransactionDetail;
import com.intech.domain.TxnRunContext;
import com.intech.exception.XmlWriteException;
import com.intech.util.DateService;
import com.intech.util.FileUtil;

/**
 * Step 5: Generates the TXN XML file.
 *
 * Updates done to match legacy:
 * - Dr/Cr counterparty nodes now filled using XCNT+KBDT (when present on TransactionDetail)
 * - UniqueTransactionNumber = TRAN_ID + PART_TRAN_SRL_NUM + ddMMyyyy(txnDate)
 * - Narratives is now a container with Narration tags (SrNo/Text attributes)
 */
@Service
public class TxnXmlWriter {

    private static final Logger log = LoggerFactory.getLogger(TxnXmlWriter.class);
    private static final String NS = "http://cpsms.com/TransactionsDataRequest";

    private static final DateTimeFormatter LEGACY_DD_MON_YYYY =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DDMMYYYY_DIGITS =
            DateTimeFormatter.ofPattern("ddMMyyyy", Locale.ENGLISH);

    private final TxnProperties txnProperties;
    private final DateService dateService;
    

    public TxnXmlWriter(TxnProperties txnProperties, DateService dateService) {
        this.txnProperties = txnProperties;
        this.dateService = dateService;
    }

    public Path write(TxnRunContext ctx, List<AccountReport> accounts) {
        Path outputDir = Path.of(txnProperties.getOutput().getDir());
        String fileName = ctx.getMessageId() + ".xml";

        Path tmp = null;
        Path fin = null;

        try {
            tmp = FileUtil.tempFile(outputDir, fileName);
            fin = FileUtil.finalFile(outputDir, fileName);

            XMLOutputFactory factory = XMLOutputFactory.newInstance();

            try (OutputStream os = Files.newOutputStream(tmp)) {
                XMLStreamWriter w = factory.createXMLStreamWriter(os, "UTF-8");

                w.writeStartDocument("UTF-8", "1.0");

                w.writeStartElement("TransactionDetails");
                w.writeDefaultNamespace(NS);
                w.writeAttribute("BankCode", txnProperties.getBank().getCode());
                w.writeAttribute("BankName", txnProperties.getBank().getName());
                w.writeAttribute("Destination", txnProperties.getRoot().getDestination());
                w.writeAttribute("MessageId", ctx.getMessageId());

                int recordsCount = txnProperties.getRoot().getRecordsCount() > 0
                        ? txnProperties.getRoot().getRecordsCount()
                        : accounts.size();

                w.writeAttribute("RecordsCount", String.valueOf(recordsCount));
                w.writeAttribute("Source", txnProperties.getRoot().getSource());

                for (AccountReport ar : accounts) {
                    writeAccount(w, ar, ctx);
                }

                w.writeEndElement(); // TransactionDetails
                w.writeEndDocument();
                w.flush();
                w.close();
            }

            FileUtil.atomicMove(tmp, fin);
            log.info("TXN XML generated: {}", fin.toAbsolutePath());
            return fin;

        } catch (Exception e) {
            try {
                if (tmp != null) Files.deleteIfExists(tmp);
            } catch (Exception ignored) { }
            throw new XmlWriteException("Failed to write TXN XML file: " + (fin != null ? fin : fileName), e);
        }
    }

    private void writeAccount(XMLStreamWriter w, AccountReport ar, TxnRunContext ctx) throws Exception {
        BalanceInfo b = ar.getBalanceInfo();

        w.writeStartElement("Account");
        w.writeAttribute("AccountNo", safe(b.getForacid()));
        w.writeAttribute("BSRCode", safe(b.getBsrCode()));

        w.writeAttribute("ClosingBalance", formatMoney(b.getClosingBalance()));
        w.writeAttribute("ClosingBalanceDate", safe(b.getCloseDateDdMmYyyySlashes()));
        w.writeAttribute("OpeningBalance", formatMoney(b.getOpeningBalance()));
        w.writeAttribute("OpeningBalanceDate", safe(b.getCloseDateDdMmYyyySlashes()));

        w.writeAttribute("ShadowClosingBalance", formatMoney(b.getClosingBalance()));
        w.writeAttribute("ShadowOpeningBalance", formatMoney(b.getOpeningBalance()));

        int txnCount = ar.getTransactionCount();
        if (txnCount > 0 && !ar.getTransactions().isEmpty()) {
            txnCount = ar.getTransactions().size();
        }
        w.writeAttribute("TransactionCount", String.valueOf(txnCount));

        if (txnCount > 0) {
            for (TransactionDetail t : ar.getTransactions()) {
                writeTransaction(w, t, ctx);
            }
        }

        w.writeEndElement(); // Account
    }

    private void writeTransaction(XMLStreamWriter w, TransactionDetail t, TxnRunContext ctx) throws Exception {
        w.writeStartElement("Transaction");

        // Date: from tran_date (dd-mon-yyyy) -> dd/MM/yyyy (best-effort)
        String date = dateService.toDdMmYyyySlashesFromDdMonYyyy(t.getTranDateDdMonYyyy());
        if (date == null || date.isBlank()) {
            date = safe(t.getValueDateDdMmYyyy());
        }

        writeNode(w, "Date", date);
        writeNode(w, "ValueDate", safe(t.getValueDateDdMmYyyy()));
        writeNode(w, "TransactionType", normalizeCrDr(t.getPartTranType()));
        writeNode(w, "TransactionCategory", safe(t.getTranSubType()));
        writeNode(w, "ChannelType", safe(txnProperties.getTxn().getChannelDefault()));

        writeNode(w, "InstrumentNo", normalizeBlankIfNA(t.getInstrumentNo()));
        writeNode(w, "InstrumentDate", normalizeBlankIfNA(t.getInstrumentDateDdMmYyyy()));

        writeNode(w, "Amount", formatMoney(t.getAmount()));
        writeNode(w, "PostTranBal", safe(txnProperties.getTxn().getPostTranBalDefault()));

        String remarks = safe(t.getRemarks());
        if (remarks.isBlank()) {
            remarks = safe(t.getTranParticular());
        }
        writeNode(w, "Remarks", remarks);

        // Dr/Cr fields: legacy filled only when XCNT==1 and KBDT returned data
        CounterpartyInfo cp = t.getCounterpartyInfo();
        if (cp != null) {
        	System.out.println("inside if,............");
        	System.out.println("no-->  " + cp.getAccountNumber());
        	System.out.println("name-->  " + cp.getAccountName());
            writeNode(w, "DrCrAccountNumber", safe(cp.getAccountNumber()));
            writeNode(w, "DrCrAccountName", safe(cp.getAccountName()));
            writeNode(w, "DrCrBnkNm", safe(cp.getBankName()));
            writeNode(w, "DrCrBnkBrCd", safe(cp.getBankBranchCode()));
        } else {
        	System.out.println("inside else,............");
        	System.out.println("Eno-->  " + txnProperties.getTxn().getDrcrAccountNoDefault());
        	System.out.println("Ename-->  " + txnProperties.getTxn().getDrcrAccountNameDefault());
            // fallback to defaults from properties
            writeNode(w, "DrCrAccountNumber", safe(txnProperties.getTxn().getDrcrAccountNoDefault()));
            writeNode(w, "DrCrAccountName", safe(txnProperties.getTxn().getDrcrAccountNameDefault()));
            writeNode(w, "DrCrBnkNm", safe(txnProperties.getTxn().getDrcrBankNameDefault()));
            writeNode(w, "DrCrBnkBrCd", safe(txnProperties.getTxn().getDrcrBankBranchCodeDefault()));
        }

        // Legacy UniqueTransactionNumber = TRAN_ID + PART_TRAN_SRL_NUM + ddMMyyyy(txnDate)
        String txnDateDigits = safe(ctx.getTxnDateDdMmYyyy());
        String uniq = safe(t.getTranId()) + safe(t.getPartTranSrlNum()) + txnDateDigits;
        writeNode(w, "UniqueTransactionNumber", uniq);

        writeNode(w, "TranRefNo", safe(txnProperties.getTxn().getTranRefNoDefault()));
        writeNode(w, "CPSMSTransactionId", safe(txnProperties.getTxn().getCpsmsTransactionIdDefault()));

        // Legacy Narratives container with Narration(SrNo, Text)
        writeNarratives(w, t);

        w.writeEndElement(); // Transaction
    }

    private void writeNarratives(XMLStreamWriter w, TransactionDetail t) throws Exception {
        w.writeStartElement("Narratives");

        int sr = 1;
        String n1 = safe(t.getTranParticular());
        if (!n1.isBlank() && !"NA".equalsIgnoreCase(n1)) {
            writeNarration(w, sr++, n1);
        }

        String n2 = safe(t.getTranParticular2());
        if (!n2.isBlank() && !"NA".equalsIgnoreCase(n2)) {
            writeNarration(w, sr++, n2);
        }

        w.writeEndElement(); // Narratives
    }

    private void writeNarration(XMLStreamWriter w, int srNo, String text) throws Exception {
        w.writeStartElement("Narration");
        w.writeAttribute("SrNo", String.valueOf(srNo));
        w.writeAttribute("Text", text);
        w.writeEndElement();
    }

    private void writeNode(XMLStreamWriter w, String name, String value) throws Exception {
        w.writeStartElement(name);
        w.writeCharacters(value == null ? "" : value);
        w.writeEndElement();
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }

    private String normalizeBlankIfNA(String v) {
        if (v == null) return "";
        String s = v.trim();
        return s.equalsIgnoreCase("NA") ? "" : s;
    }

    private String normalizeCrDr(String v) {
        if (v == null) return "";
        String s = v.trim();
        if (s.equalsIgnoreCase("C") || s.equalsIgnoreCase("CR") || s.equalsIgnoreCase("CREDIT")) return "Cr";
        if (s.equalsIgnoreCase("D") || s.equalsIgnoreCase("DR") || s.equalsIgnoreCase("DEBIT")) return "Dr";
        return s;
    }

    private String formatMoney(BigDecimal v) {
        if (v == null) return "0.00";
        return v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    // (Optional helper if you ever need ddMMyyyy from dd-mon-yyyy again)
    @SuppressWarnings("unused")
    private String ddMMyyyyFromDdMonYyyy(String ddMonYyyy) {
        try {
            if (ddMonYyyy == null || ddMonYyyy.isBlank()) return "";
            LocalDate d = LocalDate.parse(ddMonYyyy.trim(), LEGACY_DD_MON_YYYY);
            return d.format(DDMMYYYY_DIGITS);
        } catch (Exception e) {
            return "";
        }
    }
}
