package com.intech.service;

import com.intech.config.TxnProperties;
import com.intech.domain.AccountReport;
import com.intech.domain.BalanceInfo;
import com.intech.domain.TransactionDetail;
import com.intech.domain.TxnRunContext;
import com.intech.exception.XmlWriteException;
import com.intech.util.DateService;
import com.intech.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Step 5: Generates the TXN XML file.
 *
 * This writer matches the structure observed in your sample file:
 *
 * <TransactionDetails ...>
 *   <Account ... TransactionCount="0"/>
 *   <Account ... TransactionCount="N">
 *      <Transaction>
 *        <Date>...</Date>
 *        ...
 *      </Transaction>
 *   </Account>
 * </TransactionDetails>
 *
 * Rule:
 * - A Transaction node is written only when transactionCount > 0.
 */
@Service
public class TxnXmlWriter {

    private static final Logger log = LoggerFactory.getLogger(TxnXmlWriter.class);

    private static final String NS = "http://cpsms.com/TransactionsDataRequest";

    private final TxnProperties txnProperties;
    private final DateService dateService;

    public TxnXmlWriter(TxnProperties txnProperties, DateService dateService) {
        this.txnProperties = txnProperties;
        this.dateService = dateService;
    }

    /**
     * Writes the XML to outputDir with safe temp-file and atomic move.
     *
     * @return the final file path.
     */
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

                // XML header
                w.writeStartDocument("UTF-8", "1.0");

                // Root
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

                // Accounts
                for (AccountReport ar : accounts) {
                    writeAccount(w, ar);
                }

                // Close root
                w.writeEndElement();
                w.writeEndDocument();
                w.flush();
                w.close();
            }

            FileUtil.atomicMove(tmp, fin);
            log.info("TXN XML generated: {}", fin.toAbsolutePath());
            return fin;

        } catch (Exception e) {
            // Cleanup temp file if something failed
            try {
                if (tmp != null) {
                    Files.deleteIfExists(tmp);
                }
            } catch (Exception ignored) { }
            throw new XmlWriteException("Failed to write TXN XML file: " + (fin != null ? fin : fileName), e);
        }
    }

    private void writeAccount(XMLStreamWriter w, AccountReport ar) throws Exception {
        BalanceInfo b = ar.getBalanceInfo();

        w.writeStartElement("Account");
        w.writeAttribute("AccountNo", safe(b.getForacid()));
        w.writeAttribute("BSRCode", safe(b.getBsrCode()));

        w.writeAttribute("ClosingBalance", formatMoney(b.getClosingBalance()));
        w.writeAttribute("ClosingBalanceDate", safe(b.getCloseDateDdMmYyyySlashes()));
        w.writeAttribute("OpeningBalance", formatMoney(b.getOpeningBalance()));
        w.writeAttribute("OpeningBalanceDate", safe(b.getCloseDateDdMmYyyySlashes()));

        // In your sample, shadow balances are same as balances.
        w.writeAttribute("ShadowClosingBalance", formatMoney(b.getClosingBalance()));
        w.writeAttribute("ShadowOpeningBalance", formatMoney(b.getOpeningBalance()));

        int txnCount = ar.getTransactionCount();
        // For safety, prefer the fetched list size when present.
        if (txnCount > 0 && !ar.getTransactions().isEmpty()) {
            txnCount = ar.getTransactions().size();
        }
        w.writeAttribute("TransactionCount", String.valueOf(txnCount));

        // Transactions only if count > 0
        if (txnCount > 0) {
            for (TransactionDetail t : ar.getTransactions()) {
                writeTransaction(w, t);
            }
        }

        w.writeEndElement(); // Account
    }

    private void writeTransaction(XMLStreamWriter w, TransactionDetail t) throws Exception {
        w.writeStartElement("Transaction");

        // Date: from tran_date (dd-mon-yyyy) -> dd/MM/yyyy (best-effort)
        String date = dateService.toDdMmYyyySlashesFromDdMonYyyy(t.getTranDateDdMonYyyy());
        if (date == null || date.isBlank()) {
            date = safe(t.getValueDateDdMmYyyy());
        }

        writeNode(w, "Date", date);
        writeNode(w, "ValueDate", safe(t.getValueDateDdMmYyyy()));

        // TransactionType: map to 'Cr'/'Dr' if needed
        writeNode(w, "TransactionType", normalizeCrDr(t.getPartTranType()));

        // TransactionCategory: use TRAN_SUB_TYPE
        writeNode(w, "TransactionCategory", safe(t.getTranSubType()));

        // ChannelType: default from config
        writeNode(w, "ChannelType", safe(txnProperties.getTxn().getChannelDefault()));

        // Instrument
        writeNode(w, "InstrumentNo", normalizeBlankIfNA(t.getInstrumentNo()));
        writeNode(w, "InstrumentDate", normalizeBlankIfNA(t.getInstrumentDateDdMmYyyy()));

        // Amount
        writeNode(w, "Amount", formatMoney(t.getAmount()));

        // PostTranBal (not provided by query)
        writeNode(w, "PostTranBal", safe(txnProperties.getTxn().getPostTranBalDefault()));

        // Remarks: use TRAN_RMKS (fallback TRAN_PARTICULAR)
        String remarks = safe(t.getRemarks());
        if (remarks.isBlank()) {
            remarks = safe(t.getTranParticular());
        }
        writeNode(w, "Remarks", remarks);

        // Dr/Cr counterparty fields (not provided by query, keep defaults)
        writeNode(w, "DrCrAccountNumber", safe(txnProperties.getTxn().getDrcrAccountNoDefault()));
        writeNode(w, "DrCrAccountName", safe(txnProperties.getTxn().getDrcrAccountNameDefault()));
        writeNode(w, "DrCrBnkNm", safe(txnProperties.getTxn().getDrcrBankNameDefault()));
        writeNode(w, "DrCrBnkBrCd", safe(txnProperties.getTxn().getDrcrBankBranchCodeDefault()));

        // UniqueTransactionNumber: use part_tran_srl_num if present, else TRAN_ID
        String uniq = safe(t.getPartTranSrlNum());
        if (uniq.isBlank()) {
            uniq = safe(t.getTranId());
        }
        writeNode(w, "UniqueTransactionNumber", uniq);

        // Optional fields in sample (kept empty/default)
        writeNode(w, "TranRefNo", safe(txnProperties.getTxn().getTranRefNoDefault()));
        writeNode(w, "CPSMSTransactionId", safe(txnProperties.getTxn().getCpsmsTransactionIdDefault()));

        // Narratives: can carry particulars; sample often has empty narratives.
        String narratives = "";
        if (t.getTranParticular2() != null && !t.getTranParticular2().isBlank() && !"NA".equalsIgnoreCase(t.getTranParticular2().trim())) {
            narratives = t.getTranParticular2().trim();
        }
        writeNode(w, "Narratives", narratives);

        w.writeEndElement(); // Transaction
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
        if (s.equalsIgnoreCase("NA")) return "";
        return s;
    }

    private String normalizeCrDr(String v) {
        if (v == null) return "";
        String s = v.trim();
        if (s.equalsIgnoreCase("C") || s.equalsIgnoreCase("CR") || s.equalsIgnoreCase("CREDIT")) return "Cr";
        if (s.equalsIgnoreCase("D") || s.equalsIgnoreCase("DR") || s.equalsIgnoreCase("DEBIT")) return "Dr";
        // Some CBS store already as 'Cr'/'Dr'
        if (s.equalsIgnoreCase("CR")) return "Cr";
        if (s.equalsIgnoreCase("DR")) return "Dr";
        return s;
    }

    private String formatMoney(BigDecimal v) {
        if (v == null) return "0.00";
        return v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
