package com.intech.cpsms.service;

import com.intech.cpsms.config.CpsmsProps;
import com.intech.cpsms.dto.*;
import com.intech.cpsms.util.FilenameRules;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
public class ResponseBuilderService {

	// Hard-coded as per your samples
	private static final String ACK_XMLNS = "https://pfms.nic.in/PaymentRequestAck";

	private final CpsmsProps props;

	public ResponseBuilderService(CpsmsProps props) {
		this.props = props;
	}

	public void generateAndWrite(ParsedBatchDTO parsed) throws Exception {

		boolean dscOk = parsed.getDscResult() == DscResult.VALID || parsed.getDscResult() == DscResult.NOT_APPLICABLE;
		boolean ok = dscOk && parsed.isFormatValid();

		String responseMsgId = parsed.getResponseMessageId();
		if (isBlank(responseMsgId)) {
			responseMsgId = FilenameRules.responseMessageIdFromRequest(parsed.getRequestMessageId());
			parsed.setResponseMessageId(responseMsgId);
		}

		final String xml;

		if (ok && !parsed.hasPerBatchErrors()) {
			// ✅ SUCCESS ACK
			xml = createAcknowledgeWithSuccess(parsed);
		} else if (parsed.hasPerBatchErrors()) {
			// ❌ NACK with transactions (per your attached sample)
			xml = createAcknowledgeWithTransactions(parsed);
		} else {
			// ❌ Simple NACK without tx details
			xml = createAcknowledgeSimple(parsed, "E", nvl(parsed.getTopErrorCode()), nvl(parsed.getTopErrorRemarks()));
		}

		Path outDir = Path.of(props.getPaths().getRes());
		Files.createDirectories(outDir);
		Path out = outDir.resolve(responseMsgId + ".xml");

		Files.writeString(out, xml, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
	}

	// =========================================================
	// SUCCESS ACK (already aligned to your rules)
	// =========================================================

	private String createAcknowledgeWithSuccess(ParsedBatchDTO parsed) {
		String reqMsgId = nvl(parsed.getRequestMessageId());
		String ackMsgId = nvl(parsed.getResponseMessageId());
		String paymentProduct = nvl(parsed.getPaymentProduct());

		String bankCode = resolveBankCode(parsed, reqMsgId);
		String bankName = nvl(parsed.getBankName());

		int recordsCount = parsed.getRecordCount() != null ? parsed.getRecordCount() : countCredits(parsed);

		int recordsFound = countCredits(parsed);

		String batchNo = nvl(parsed.getBatchNumber());

		String firstDebitTranId = findFirstDebitTranId(parsed);
		if (isBlank(firstDebitTranId)) {
			firstDebitTranId = batchNo;
		}

		StringBuilder sb = new StringBuilder(1024);

		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
		sb.append("<Acknowledgement");
		sb.append(" xmlns=\"").append(ACK_XMLNS).append("\"");
		sb.append(" BankCode=\"").append(escape(bankCode)).append("\"");
		sb.append(" BankName=\"").append(escape(bankName)).append("\"");
		sb.append(" Destination=\"CPSMS\"");
		sb.append(" MessageId=\"").append(escape(ackMsgId)).append("\"");
		sb.append(" PaymentProduct=\"").append(escape(paymentProduct)).append("\"");
		sb.append(" Source=\"").append(escape(bankCode)).append("\">");

		sb.append("<Ack>");
		sb.append("<OriginalMessageId>").append(escape(reqMsgId)).append("</OriginalMessageId>");
		sb.append("<RecordsCount>").append(recordsCount).append("</RecordsCount>");
		sb.append("<RecordsFound>").append(recordsFound).append("</RecordsFound>");

		sb.append("<Batch>");
		sb.append("<CPSMSBatchNo>").append(escape(batchNo)).append("</CPSMSBatchNo>");
		sb.append("<ResponseCode>S</ResponseCode>");
		sb.append("<ErrorCode/>");

		// 1st RecordError: debit tran (or batchNo)
		if (!isBlank(firstDebitTranId)) {
			sb.append("<RecordError CPSMSTranId=\"").append(escape(firstDebitTranId)).append("\" ErrorCode=\"\"/>");
		}

		// Remaining: each credit tran
		appendCreditRecordErrors(parsed, sb, "");

		sb.append("</Batch>");
		sb.append("</Ack>");
		sb.append("</Acknowledgement>");

		return sb.toString();
	}

	// =========================================================
	// NACK WITH TRANSACTIONS (this is what you asked for)
	// =========================================================

	/**
	 * NACK with transactions: - Same structure as success ACK -
	 * <ResponseCode>E</ResponseCode> - <ErrorCode>{scenarioError}</ErrorCode> in
	 * Batch (from parsed.getTopErrorCode()) - <RecordError>: 1st: CPSMSTranId =
	 * CPSMSDebitTranId / batchNo, ErrorCode = same scenarioError Next: CPSMSTranId
	 * = each CPSMSCreditTranId, ErrorCode = same scenarioError
	 */
	private String createAcknowledgeWithTransactions(ParsedBatchDTO parsed) {
		String reqMsgId = nvl(parsed.getRequestMessageId());
		String ackMsgId = nvl(parsed.getResponseMessageId());
		String paymentProduct = nvl(parsed.getPaymentProduct());

		String bankCode = resolveBankCode(parsed, reqMsgId);
		String bankName = nvl(parsed.getBankName());

		// ErrorCode from validation logic (like "N001", "D025")
		String errorCode = nvl(parsed.getTopErrorCode());
		if (isBlank(errorCode)) {
			errorCode = "E999"; // fallback; adjust to your standard if needed
		}

		int recordsCount = parsed.getRecordCount() != null ? parsed.getRecordCount() : countCredits(parsed);

		int recordsFound = countCredits(parsed);

		String batchNo = nvl(parsed.getBatchNumber());

		String firstDebitTranId = findFirstDebitTranId(parsed);
		if (isBlank(firstDebitTranId)) {
			firstDebitTranId = batchNo;
		}

		StringBuilder sb = new StringBuilder(1024);

		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
		sb.append("<Acknowledgement");
		sb.append(" xmlns=\"").append(ACK_XMLNS).append("\"");
		sb.append(" BankCode=\"").append(escape(bankCode)).append("\"");
		sb.append(" BankName=\"").append(escape(bankName)).append("\"");
		sb.append(" Destination=\"CPSMS\"");
		sb.append(" MessageId=\"").append(escape(ackMsgId)).append("\"");
		sb.append(" PaymentProduct=\"").append(escape(paymentProduct)).append("\"");
		sb.append(" Source=\"").append(escape(bankCode)).append("\">");

		sb.append("<Ack>");
		sb.append("<OriginalMessageId>").append(escape(reqMsgId)).append("</OriginalMessageId>");
		sb.append("<RecordsCount>").append(recordsCount).append("</RecordsCount>");
		sb.append("<RecordsFound>").append(recordsFound).append("</RecordsFound>");

		sb.append("<Batch>");
		sb.append("<CPSMSBatchNo>").append(escape(batchNo)).append("</CPSMSBatchNo>");
		sb.append("<ResponseCode>E</ResponseCode>");
		sb.append("<ErrorCode>").append(escape(errorCode)).append("</ErrorCode>");

		// 1st RecordError: debitTranId/batchNo with ErrorCode
		if (!isBlank(firstDebitTranId)) {
			sb.append("<RecordError CPSMSTranId=\"").append(escape(firstDebitTranId)).append("\" ErrorCode=\"")
					.append(escape(errorCode)).append("\"/>");
		}

		// Remaining: each creditTran with same ErrorCode
		appendCreditRecordErrors(parsed, sb, errorCode);

		sb.append("</Batch>");
		sb.append("</Ack>");
		sb.append("</Acknowledgement>");

		return sb.toString();
	}

	// =========================================================
	// SIMPLE NACK (no tx details) - safe default
	// =========================================================

	private String createAcknowledgeSimple(ParsedBatchDTO parsed, String responseCode, String errorCode,
			String remarks) {

		String reqMsgId = nvl(parsed.getRequestMessageId());
		String ackMsgId = nvl(parsed.getResponseMessageId());
		String paymentProduct = nvl(parsed.getPaymentProduct());

		String bankCode = resolveBankCode(parsed, reqMsgId);
		String bankName = nvl(parsed.getBankName());

		int recordsCount = parsed.getRecordCount() != null ? parsed.getRecordCount() : countCredits(parsed);

		int recordsFound = 0; // usually 0 for hard reject

		String batchNo = nvl(parsed.getBatchNumber());
		String ec = nvl(errorCode);

		StringBuilder sb = new StringBuilder(512);
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
		sb.append("<Acknowledgement");
		sb.append(" xmlns=\"").append(ACK_XMLNS).append("\"");
		sb.append(" BankCode=\"").append(escape(bankCode)).append("\"");
		sb.append(" BankName=\"").append(escape(bankName)).append("\"");
		sb.append(" Destination=\"CPSMS\"");
		sb.append(" MessageId=\"").append(escape(ackMsgId)).append("\"");
		sb.append(" PaymentProduct=\"").append(escape(paymentProduct)).append("\"");
		sb.append(" Source=\"").append(escape(bankCode)).append("\">");

		sb.append("<Ack>");
		sb.append("<OriginalMessageId>").append(escape(reqMsgId)).append("</OriginalMessageId>");
		sb.append("<RecordsCount>").append(recordsCount).append("</RecordsCount>");
		sb.append("<RecordsFound>").append(recordsFound).append("</RecordsFound>");

		sb.append("<Batch>");
		sb.append("<CPSMSBatchNo>").append(escape(batchNo)).append("</CPSMSBatchNo>");
		sb.append("<ResponseCode>").append(escape(responseCode)).append("</ResponseCode>");
		sb.append("<ErrorCode>").append(escape(ec)).append("</ErrorCode>");
		sb.append("</Batch>");

		sb.append("</Ack>");
		sb.append("</Acknowledgement>");

		return sb.toString();
	}

	// =========================================================
	// Helpers
	// =========================================================

	private int countCredits(ParsedBatchDTO parsed) {
		if (parsed.getDebits() == null)
			return 0;
		int count = 0;
		for (ParsedDebitDTO d : parsed.getDebits()) {
			if (d.getCredits() != null) {
				count += d.getCredits().size();
			}
		}
		return count;
	}

	private String findFirstDebitTranId(ParsedBatchDTO parsed) {
		if (parsed.getDebits() == null || parsed.getDebits().isEmpty())
			return null;
		ParsedDebitDTO d = parsed.getDebits().get(0);
		return d.getCpsmsDebitTranId();
	}

	private void appendCreditRecordErrors(ParsedBatchDTO parsed, StringBuilder sb, String errorCode) {
		if (parsed.getDebits() == null)
			return;
		for (ParsedDebitDTO d : parsed.getDebits()) {
			if (d.getCredits() == null)
				continue;
			for (ParsedCreditDTO c : d.getCredits()) {
				String creditTran = c.getCpsmsCreditTran();
				if (!isBlank(creditTran)) {
					sb.append("<RecordError CPSMSTranId=\"").append(escape(creditTran)).append("\" ErrorCode=\"")
							.append(escape(errorCode)).append("\"/>");
				}
			}
		}
	}

	private String resolveBankCode(ParsedBatchDTO parsed, String reqMsgId) {
		if (!isBlank(parsed.getBankCode())) {
			return parsed.getBankCode().trim();
		}
		if (reqMsgId != null && reqMsgId.length() >= 3) {
			return reqMsgId.substring(0, 3);
		}
		return "";
	}

	private static String nvl(String s) {
		return s == null ? "" : s;
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	private static String escape(String s) {
		if (s == null)
			return "";
		StringBuilder out = new StringBuilder(s.length() + 16);
		for (char c : s.toCharArray()) {
			switch (c) {
			case '&':
				out.append("&amp;");
				break;
			case '<':
				out.append("&lt;");
				break;
			case '>':
				out.append("&gt;");
				break;
			case '"':
				out.append("&quot;");
				break;
			case '\'':
				out.append("&apos;");
				break;
			default:
				out.append(c);
			}
		}
		return out.toString();
	}
}
