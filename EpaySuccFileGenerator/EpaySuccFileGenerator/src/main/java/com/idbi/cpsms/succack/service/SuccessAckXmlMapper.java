package com.idbi.cpsms.succack.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.idbi.cpsms.succack.bean.SuccessPayments;
import com.idbi.cpsms.succack.bean.SuccessPayments.BatchDetails;
import com.idbi.cpsms.succack.bean.SuccessPayments.BatchDetails.SuccessPayment;
import com.idbi.cpsms.succack.service.dto.BatchInfo;
import com.idbi.cpsms.succack.service.dto.CreditInfo;

/**
 * Builds the SuccessPayments JAXB object from DB DTOs. Tag mapping is based on
 * your old PaySuccessRept logic + screenshots.
 */
@Component
public class SuccessAckXmlMapper {

	private final Properties props;
	private final DateTimeFormatter tsFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	public SuccessAckXmlMapper(@Qualifier("successAckProperties") Properties successAckProperties) {
		this.props = successAckProperties;
	}

	public SuccessPayments buildXml(BatchInfo batch, List<CreditInfo> credits, String messageId) {

		SuccessPayments root = new SuccessPayments();

		String bankCode = batch.getBankCode();
		int recordCount=batch.getRecordsCount();
		String bankName = props.getProperty("success-ack.bank-name", "IDBI BANK");
		root.setXmlns("http://cpsms.com/SuccessPaymentData");
		root.setMessageId(messageId);
		root.setBankCode(bankCode); // attribute BankCode
		root.setSource(bankCode); // Source = bankCode
		root.setDestination("CPSMS");
		root.setPaymentProduct(batch.getPaymentProd());
		root.setBankName(bankName);
		root.setRecordsCount(recordCount);

		BatchDetails bd = new BatchDetails();
		bd.setCPSMSBatchNo(batch.getBatchNumber());
		bd.setC5185(String.valueOf(credits.size())); // number of SuccessPayment records

		for (CreditInfo c : credits) {
			SuccessPayment sp = new SuccessPayment();

			// C2020 - Initiating UTR or FtId or DebitTranId
			sp.setC2020(resolveC2020(c));

			// C2006 - CPSMS Credit Tran
			sp.setC2006(nullToSpace(c.getCpsmsCreditTran()));
			
			// C6061 - Account Number
			sp.setC6061(resolveAccountNumber(c));
			
			// UID - UID or blank
			sp.setUID(resolveUid(c));
			
			// BankIIN - using APBS rules
			sp.setBankIIN(resolveBankIin(c));

			// C5518 - Credit IFSC
			sp.setC5518(nullToSpace(c.getCreditIfsc()));


			// C6081 - Account Name
			sp.setC6081(nullToSpace(c.getCreditAccountName()));

			// C4038 - Amount
			sp.setC4038(resolveAmount(c.getCreditAmount()));

			// C6346 - Success / Deemed Success / DSUC
			sp.setC6346(resolveStatus(c));

			// C3501 - DateTime yyyymmddHH24miss
			sp.setC3501(resolveDateTime(c.getTranDateFt()));

			// PmtRoute - NEFT/RTGS/ICBS/APBS/NACH
			sp.setPmtRoute(resolvePmtRoute(c.getPmtMtd()));

			bd.getSuccessPayment().add(sp);
		}

		root.setBatchDetails(bd);
		return root;
	}

	private String resolveC2020(CreditInfo c) {
		if (notBlank(c.getInitiatingUtr()))
			return c.getInitiatingUtr();
		if (notBlank(c.getTranIdFt()))
			return c.getTranIdFt();
		if (notBlank(c.getDebitTranId()))
			return c.getDebitTranId();
		return " ";
	}

	private String resolveAccountNumber(CreditInfo c) {
		String acc = c.getCreditAccountNumber();
		return notBlank(acc) ? acc : " ";
	}

	private String resolveBankIin(CreditInfo c) {
		String route = resolvePmtRoute(c.getPmtMtd()); // NEFT/RTGS/ICBS/APBS/NACH
		String bankIin = c.getCreditBankIin();
		String ifsc = c.getCreditIfsc();

		if (bankIin == null || bankIin.trim().isEmpty()) {
			bankIin = "000000";
		}

		// Recreate old APBS mapping:
		if ("APBS".equalsIgnoreCase(route) && "000000".equals(bankIin) && ifsc != null) {
			if (ifsc.startsWith("SBIN00")) {
				bankIin = "508548";
			} else if (ifsc.startsWith("BKDN")) {
				bankIin = "508547";
			} else if (ifsc.startsWith("DCBL")) {
				bankIin = "607290";
			} else if (ifsc.startsWith("HDFC")) {
				bankIin = "607152";
			}
		} else if (!"APBS".equalsIgnoreCase(route) && "000000".equals(bankIin)) {
			// For non-APBS, 000000 becomes blank (as in legacy fallback)
			bankIin = " ";
		}

		return bankIin;
	}

	private String resolveUid(CreditInfo c) {
		String uid = c.getCreditUid();
		return uid != null ? uid : " ";
	}

	private String resolveAmount(BigDecimal amt) {
		if (amt == null)
			return "0";
		return amt.stripTrailingZeros().toPlainString();
	}

	private String resolveStatus(CreditInfo c) {
		String cs = c.getCreditStatus() != null ? c.getCreditStatus().trim() : "";
		String mtd = c.getPmtMtd() != null ? c.getPmtMtd().trim() : "";

		// From legacy: I + (N/T) => SUCC, A => SUCC else DSUC
		if ("I".equalsIgnoreCase(cs) && ("N".equalsIgnoreCase(mtd) || "T".equalsIgnoreCase(mtd))) {
			return "SUCC";
		}
		if ("A".equalsIgnoreCase(cs)) {
			return "SUCC";
		}
		return "DSUC";
	}

	private String resolveDateTime(Timestamp ts) {
		if (ts == null)
			return "00000000000000";
		LocalDateTime ldt = ts.toLocalDateTime();
		return ldt.format(tsFormat);
	}

	private String resolvePmtRoute(String mtd) {
		if (mtd == null)
			return "NEFT";
		switch (mtd.trim().toUpperCase()) {
		case "N":
			return "NEFT";
		case "R":
			return "RTGS";
		case "T":
			return "ICBS";
		case "APBS":
			return "APBS";
		case "NACH":
			return "NACH";
		default:
			return "NEFT";
		}
	}

	private String nullToSpace(String s) {
		return (s == null) ? " " : s;
	}

	private boolean notBlank(String s) {
		return s != null && !s.trim().isEmpty();
	}
}
