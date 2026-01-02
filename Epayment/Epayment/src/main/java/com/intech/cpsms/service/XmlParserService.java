package com.intech.cpsms.service;

import com.intech.cpsms.dto.*;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Parses the CPSMS Payment Request XML into ParsedBatchDTO.
 *
 * Key mappings from XML → DTO fields: - <Payments ...> attributes: MessageId →
 * dto.requestMessageId PaymentProduct → dto.paymentProduct (UPPER) RecordsCount
 * → dto.recordsCount - <BatchDetails ...> attributes: CPSMSBatchNo →
 * dto.batchNumber + add a ParsedBatchBlockDTO with cpsmsBatchNo C3535, C1106 →
 * (kept in block for reference if needed; often batch time / record count) -
 * <DebitAccounts>/<DebitAccount ...> attributes: C4063 → debit.debitAmount
 * C2020 → (batch no) [Agency IFSC/AccNo/Name] — your sample shows coded names;
 * if you later know the exact ones, plug them below (see TODOs).
 * <FileOrgtr><C7002>…</C7002>…</FileOrgtr> kept concatenated into debit.remarks
 * - <CreditAccounts>/<CreditAccount ...> attributes: C4038 →
 * credit.creditAmount C5569 → credit.creditIfsc C6061 →
 * credit.creditAccountNumber C6081 → credit.creditAccountName CPSMSTranId →
 * credit.cpsmsCreditTran PmtMtd → credit.pmtMtd
 * <RmtInf><C7495>…</C7495>…</RmtInf> → joined into credit.rmtInf
 *
 * Signature presence: - we mark dto.signaturePresent = true if <ds:Signature>
 * exists (namespace aware).
 *
 * Format validity: - set true by default; attach errors to dto.globalErrors if
 * you detect violations.
 */
@Service
public class XmlParserService {

	// CPSMS namespace as seen in your file
	private static final String NS_CPSMS = "http://cpsms.com/PaymentRequest";
	private static final String NS_DSIG = "http://www.w3.org/2000/09/xmldsig#";

	// if you later need date parsing for any attribute values
	private static final List<String> DATE_PATTERNS = List.of("dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy", "yyyyMMdd");

	public ParsedBatchDTO parse(Path xmlFile) throws Exception {
		if (!Files.isRegularFile(xmlFile)) {
			throw new IllegalArgumentException("Not a file: " + xmlFile);
		}

		Document doc = secureParse(xmlFile);

		// ---- Payments (root) ----
		Element payments = doc.getDocumentElement(); // <Payments ...> in NS_CPSMS
		if (payments == null || !NS_CPSMS.equals(payments.getNamespaceURI())) {
			throw new IllegalArgumentException("Root element <Payments> with CPSMS namespace not found");
		}

		String messageId = payments.getAttribute("MessageId");
		String paymentProd = upperOrEmpty(payments.getAttribute("PaymentProduct"));
		String recordsCount = payments.getAttribute("RecordsCount"); // if present

		boolean signaturePresent = doc.getElementsByTagNameNS(NS_DSIG, "Signature").getLength() > 0;

		ParsedBatchDTO dto = new ParsedBatchDTO();
		dto.setRequestMessageId(messageId);
		dto.setPaymentProduct(paymentProd);
//		dto.setRecordsCount(nullIfBlank(recordsCount));
		dto.setRecordCount(parseIntSafe(recordsCount)); // ✅ use this

		dto.setSignaturePresent(signaturePresent);
		dto.setFormatValid(true); // default; we’ll flip if validations fail

		// ---- BatchDetails ----
		NodeList batchDetails = payments.getElementsByTagNameNS(NS_CPSMS, "BatchDetails");
		if (batchDetails != null && batchDetails.getLength() > 0) {
			// If more than one, you may want to validate they agree on CPSMSBatchNo
			String firstBatchNo = null;

			for (int i = 0; i < batchDetails.getLength(); i++) {
				Element batchEl = (Element) batchDetails.item(i);
				String cpsmsBatchNo = batchEl.getAttribute("CPSMSBatchNo");
				String c3535 = batchEl.getAttribute("C3535"); // often batch time or code
				String c1106 = batchEl.getAttribute("C1106"); // often record count

				String bankCode = payments.getAttribute("BankCode");
				String bankName = payments.getAttribute("BankName");

				dto.setBankCode(bankCode);
				dto.setBankName(bankName);

				ParsedBatchBlockDTO blk = new ParsedBatchBlockDTO();
//                blk.setCpsmsBatchNo(nullIfBlank(cpsmsBatchNo));
				blk.setBatchNumber(nullIfBlank(cpsmsBatchNo));
				// keep raw coded fields for traceability if needed by downstream
				// (add setters in ParsedBatchBlockDTO if you want to persist/log them)
				// blk.setC3535(nullIfBlank(c3535));
				// blk.setC1106(nullIfBlank(c1106));

				// maintain dto.batches list
				if (dto.getBatches() == null)
					dto.setBatches(new ArrayList<>());
				dto.getBatches().add(blk);

				if (firstBatchNo == null && !isBlank(cpsmsBatchNo)) {
					firstBatchNo = cpsmsBatchNo.trim();
				}

				// ---- DebitAccounts/DebitAccount ----
				Element debitAccounts = firstChild(batchEl, NS_CPSMS, "DebitAccounts");
				if (debitAccounts != null) {
					NodeList debitNodes = debitAccounts.getElementsByTagNameNS(NS_CPSMS, "DebitAccount");
					for (int d = 0; d < debitNodes.getLength(); d++) {
						Element debitEl = (Element) debitNodes.item(d);
						ParsedDebitDTO debit = parseDebit(debitEl);
						/*
						 * log.info("PARSE: msgId={}, product={}, batchNo={}, records={}, sigPresent={}"
						 * , dto.getRequestMessageId(), dto.getPaymentProduct(), dto.getBatchNumber(),
						 * dto.getRecordsCount(), dto.isSignaturePresent());
						 */

						if (dto.getDebits() == null)
							dto.setDebits(new ArrayList<>());
						dto.getDebits().add(debit);
					}

				}

				// ---- CreditAccounts/CreditAccount ----
				Element creditAccounts = firstChild(batchEl, NS_CPSMS, "CreditAccounts");
				if (creditAccounts != null) {
					NodeList creditNodes = creditAccounts.getElementsByTagNameNS(NS_CPSMS, "CreditAccount");
					// For each debit block there may be multiple credits
					// Attach to the LAST debit we added (typical structure: one debit → many
					// credits)
					ParsedDebitDTO attachTo = (dto.getDebits() != null && !dto.getDebits().isEmpty())
							? dto.getDebits().get(dto.getDebits().size() - 1)
							: null;

					for (int c = 0; c < creditNodes.getLength(); c++) {
						Element creditEl = (Element) creditNodes.item(c);
						ParsedCreditDTO credit = parseCredit(creditEl);

						if (attachTo != null) {
							if (attachTo.getCredits() == null)
								attachTo.setCredits(new ArrayList<>());
							attachTo.getCredits().add(credit);
						} else {
							// Unusual, but don’t lose data: create a synthetic debit container
							ParsedDebitDTO synthetic = new ParsedDebitDTO();
							synthetic.setCredits(new ArrayList<>(List.of(credit)));
							if (dto.getDebits() == null)
								dto.setDebits(new ArrayList<>());
							dto.getDebits().add(synthetic);
						}
					}
				}
			}

			// Set dto.batchNumber from the first BatchDetails.CPSMSBatchNo (<= 16)
			if (!isBlank(firstBatchNo)) {
				String bn = firstBatchNo.trim();
				if (bn.length() > 16) {
					// strict: flip formatValid and attach an error (recommended)
					dto.setFormatValid(false);
					addGlobalError(dto, "N_LEN", "CPSMSBatchNo exceeds 16 chars");
					// or lenient: bn = bn.substring(0, 16);
				}
				dto.setBatchNumber(bn.length() > 16 ? bn.substring(0, 16) : bn);
			} else {
				// Missing BatchNo is a format error
				dto.setFormatValid(false);
				addGlobalError(dto, "N_MISS", "CPSMSBatchNo missing in <BatchDetails>");
			}
		} else {
			dto.setFormatValid(false);
			addGlobalError(dto, "N_MISS", "<BatchDetails> not found");
		}

		if (dto.getDebits() != null && !dto.getDebits().isEmpty()) {
			Date firstDebitDate = dto.getDebits().get(0).getDebitDate();
			if (firstDebitDate != null) {
				dto.setDebitDate(firstDebitDate); // add field + getter/setter in ParsedBatchDTO
				System.out.println("PARSE BATCH: using debitDate=" + firstDebitDate + " for batch");
			}
		}

		// Example cross-check: if RecordsCount present, compare with credits total
		// (uncomment if you want strict validation)
		// int declared = parseIntSafe(dto.getRecordsCount(), -1);
		// int actual = totalCredits(dto);
		// if (declared >= 0 && declared != actual) {
		// dto.setFormatValid(false);
		// addGlobalError(dto, "N_CNT", "RecordsCount mismatch: declared=" + declared +
		// ", actual=" + actual);
		// }

		return dto;
	}

	// ---------- element parsers ----------
	private ParsedDebitDTO parseDebit(Element debitEl) {
		ParsedDebitDTO d = new ParsedDebitDTO();

		// Amount
		d.setDebitAmount(parseBigDecimal(getAttr(debitEl, "C4063"))); // NUMBER(21,2)

		// 🔹 Agency details on DEBIT
		d.setAgencyIfsc(getAttr(debitEl, "C5756")); // IFSC (length 11)
		d.setAgencyAccountNumber(getAttr(debitEl, "C6021")); // A/C No (length 35)
		d.setAgencyAccountName(getAttr(debitEl, "C6091")); // A/C Name (length 50)
		d.setRemarks(null);

		String c3380 = getAttr(debitEl, "C3380");
		Date debitDate = parseDateC3380(c3380);
		d.setDebitDate(debitDate);
		System.out.println("PARSE DEBIT: C3380=" + c3380 + ", parsedDate=" + debitDate);

		d.setCpsmsDebitTranId(getAttr(debitEl, "C2020"));

		// Optional: <FileOrgtr><C7002>…</C7002>…</FileOrgtr>
		Element fileOrgtr = firstChild(debitEl, NS_CPSMS, "FileOrgtr");
		if (fileOrgtr != null) {
			NodeList c7002s = fileOrgtr.getElementsByTagNameNS(NS_CPSMS, "C7002");
			List<String> parts = new ArrayList<>();
			for (int i = 0; i < c7002s.getLength(); i++) {
				parts.add(textOf((Element) c7002s.item(i)));
			}
			if (!parts.isEmpty()) {
				d.setRemarks(appendRemark(d.getRemarks(), "FileOrgtr=" + String.join("|", parts)));
			}
		}

		return d;
	}

	private ParsedCreditDTO parseCredit(Element creditEl) {
		ParsedCreditDTO c = new ParsedCreditDTO();

		// Amount / IFSC / A/C No / Name
		c.setCreditAmount(parseBigDecimal(getAttr(creditEl, "C4038"))); // map to BigDecimal setter if you have one
		c.setCreditIfsc(getAttr(creditEl, "C5569"));
		c.setCreditAccountNumber(getAttr(creditEl, "C6061"));
		c.setCreditAccountName(getAttr(creditEl, "C6081"));

		// CPSMSTranId, PmtMtd
		c.setCpsmsCreditTran(getAttr(creditEl, "CPSMSTranId"));
		c.setPmtMtd(getAttr(creditEl, "PmtMtd"));

		// RmtInf -> join multiple C7495 values
		Element rmt = firstChild(creditEl, NS_CPSMS, "RmtInf");
		if (rmt != null) {
			NodeList lines = rmt.getElementsByTagNameNS(NS_CPSMS, "C7495");
			List<String> lst = new ArrayList<>();
			for (int i = 0; i < lines.getLength(); i++) {
				lst.add(textOf((Element) lines.item(i)));
			}
			if (!lst.isEmpty())
				c.setRmtInf(String.join("|", lst));
		}

		return c;
	}

	// ---------- helpers ----------

	private Document secureParse(Path xmlFile) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		// XXE hardening
		try {
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		} catch (Exception ignored) {
		}
		try {
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
		} catch (Exception ignored) {
		}
		try {
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		} catch (Exception ignored) {
		}
		try {
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (Exception ignored) {
		}
		try {
			dbf.setXIncludeAware(false);
		} catch (Exception ignored) {
		}
		try {
			dbf.setExpandEntityReferences(false);
		} catch (Exception ignored) {
		}

		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(xmlFile.toFile());
	}

	private static Element firstChild(Element parent, String ns, String local) {
		NodeList nl = parent.getElementsByTagNameNS(ns, local);
		return (nl != null && nl.getLength() > 0) ? (Element) nl.item(0) : null;
	}

	private static String textOf(Element el) {
		if (el == null)
			return null;
		String t = el.getTextContent();
		return isBlank(t) ? null : t.trim();
	}

	private static String nullIfBlank(String s) {
		return isBlank(s) ? null : s.trim();
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	private static String upperOrEmpty(String s) {
		return isBlank(s) ? "" : s.trim().toUpperCase(Locale.ROOT);
	}

	private static String getAttr(Element el, String name) {
		if (el == null)
			return null;
		String v = el.getAttribute(name);
		return isBlank(v) ? null : v.trim();
	}

	private static String appendRemark(String base, String add) {
		if (isBlank(add))
			return base;
		if (isBlank(base))
			return add;
		return base + "; " + add;
	}

	private static int parseIntSafe(String s, int def) {
		try {
			return isBlank(s) ? def : Integer.parseInt(s.trim());
		} catch (Exception e) {
			return def;
		}
	}

	private static BigDecimal parseBigDecimal(String s) {
		try {
			return isBlank(s) ? null : new BigDecimal(s.trim());
		} catch (Exception e) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static Date parseDateSafe(String s) {
		if (isBlank(s))
			return null;
		for (String p : DATE_PATTERNS) {
			try {
				return new SimpleDateFormat(p, Locale.ROOT).parse(s.trim());
			} catch (ParseException ignored) {
			}
		}
		return null;
	}

	private static void addGlobalError(ParsedBatchDTO dto, String code, String msg) {
		if (dto.getGlobalErrors() == null)
			dto.setGlobalErrors(new ArrayList<>());
		ErrorDetailDTO e = new ErrorDetailDTO();
		e.setErrorCode(code);
		e.setErrorMessage(msg);
		dto.getGlobalErrors().add(e);
	}

	private static int totalCredits(ParsedBatchDTO dto) {
		if (dto.getDebits() == null)
			return 0;
		int total = 0;
		for (ParsedDebitDTO d : dto.getDebits()) {
			if (d != null && d.getCredits() != null)
				total += d.getCredits().size();
		}
		return total;
	}

	private static Integer parseIntSafe(String s) {
		try {
			return (s == null || s.isBlank()) ? null : Integer.valueOf(s.trim());
		} catch (Exception e) {
			return null;
		}
	}

	private static Date parseDateC3380(String s) {
		if (s == null || s.isBlank())
			return null;
		String v = s.trim();
		String[] patterns = { "ddMMyyyy", "yyyyMMdd" }; // try both
		for (String p : patterns) {
			try {
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(p);
				sdf.setLenient(false);
				return sdf.parse(v);
			} catch (Exception ignored) {
			}
		}
		// If invalid, return null; if you want, you can mark formatValid=false here.
		return null;
	}

}
