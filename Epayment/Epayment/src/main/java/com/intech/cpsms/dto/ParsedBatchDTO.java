package com.intech.cpsms.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;

public class ParsedBatchDTO {

	// --- Header (from <Payments ...>)
	private String requestMessageId;
	private String paymentProduct; // EPA | DSC | CDDO | PAO | others
	private String recordsCount; // keep as String; convert later if needed
	private String corporateId;
	private String authmod;
	private Date createdDate;
	private Date modifiedDate;
	private String makerId;
	private String checkerId;
	private String remarks;
	private String batchNumber; // normalized value we’ll persist (<=16)
	// com.intech.cpsms.dto.ParsedBatchDTO
	private String responseMessageId;
	private Long batchTime;
	private Integer recordCount;
	private Date debitDate;
	private String authMaker;
	private String authChecker;
	private String bankCode; // from <Payments BankCode="..."> or derived
	private String bankName;

	// --- Signature / validation context
	private boolean signaturePresent;
	private DscResult dscResult = DscResult.NOT_APPLICABLE; // VALID/INVALID/DSCTAGNOTFOUND/NOT_APPLICABLE
	private boolean formatValid = true; // set by parser validations

	// --- Batch & records
	private List<ParsedBatchBlockDTO> batches = new ArrayList<>();
	private List<ParsedDebitDTO> debits = new ArrayList<>();

	// --- Global errors (e.g., N018 filename vs msgId, N002 duplicate)
	private List<ErrorDetailDTO> globalErrors = new ArrayList<>();

	// ---------- convenience logic ----------

	/** PaymentProduct requires DSC? */
	/*
	 * public boolean isDscApplicable() { if (paymentProduct == null) return false;
	 * String p = paymentProduct.trim().toUpperCase(); return "EPA".equals(p) ||
	 * "DSC".equals(p) || "CDDO".equals(p) || "PAO".equals(p); }
	 */

	// ParsedBatchDTO.java
	public boolean isDscApplicable() {
		// PPA does NOT require DSC; all others (EPA/DSC/CDDO/PAO) do.
		String p = paymentProduct.trim().toUpperCase();
		return "EPA".equals(p) || "DSC".equals(p) || "CDDO".equals(p) || "PAO".equals(p);
	}

	/** Any batch/debit/credit/global errors? */
	public boolean hasPerBatchErrors() {
		if (globalErrors != null && !globalErrors.isEmpty())
			return true;
		if (batches != null) {
			for (ParsedBatchBlockDTO b : batches) {
				if (b != null && b.getError() != null)
					return true;
			}
		}
		if (debits != null) {
			for (ParsedDebitDTO d : debits) {
				if (d == null)
					continue;
				if (d.getError() != null)
					return true;
				if (d.getCredits() != null) {
					for (ParsedCreditDTO c : d.getCredits()) {
						if (c != null && c.getError() != null)
							return true;
					}
				}
			}
		}
		return false;
	}

	/** First error code found (global → batch → debit → credit). */
	public String getTopErrorCode() {
		if (globalErrors != null) {
			for (ErrorDetailDTO e : globalErrors)
				if (e != null && e.getErrorCode() != null)
					return e.getErrorCode();
		}
		if (batches != null) {
			for (ParsedBatchBlockDTO b : batches) {
				if (b != null && b.getError() != null && b.getError().getErrorCode() != null)
					return b.getError().getErrorCode();
			}
		}
		if (debits != null) {
			for (ParsedDebitDTO d : debits) {
				if (d != null && d.getError() != null && d.getError().getErrorCode() != null)
					return d.getError().getErrorCode();
				if (d != null && d.getCredits() != null) {
					for (ParsedCreditDTO c : d.getCredits()) {
						if (c != null && c.getError() != null && c.getError().getErrorCode() != null)
							return c.getError().getErrorCode();
					}
				}
			}
		}
		return null;
	}

	/** First error remarks found (global → batch → debit → credit). */
	public String getTopErrorRemarks() {
		if (globalErrors != null) {
			for (ErrorDetailDTO e : globalErrors)
				if (e != null && e.getErrorMessage() != null)
					return e.getErrorMessage();
		}
		if (batches != null) {
			for (ParsedBatchBlockDTO b : batches) {
				if (b != null && b.getError() != null && b.getError().getErrorMessage() != null)
					return b.getError().getErrorMessage();
			}
		}
		if (debits != null) {
			for (ParsedDebitDTO d : debits) {
				if (d != null && d.getError() != null && d.getError().getErrorMessage() != null)
					return d.getError().getErrorMessage();
				if (d != null && d.getCredits() != null) {
					for (ParsedCreditDTO c : d.getCredits()) {
						if (c != null && c.getError() != null && c.getError().getErrorMessage() != null)
							return c.getError().getErrorMessage();
					}
				}
			}
		}
		return null;
	}

	/** Decide which ACK/NACK variant to produce. */
	public AckVariant decideAckVariant() {
		boolean dscOk = (dscResult == DscResult.VALID) || (dscResult == DscResult.NOT_APPLICABLE);
		boolean success = dscOk && formatValid;
		return success ? AckVariant.SUCCESS : (hasPerBatchErrors() ? AckVariant.NACK_WITH_TX : AckVariant.NACK_SIMPLE);
	}

	// ---------- getters & setters ----------

	public String getRequestMessageId() {
		return requestMessageId;
	}

	public void setRequestMessageId(String requestMessageId) {
		this.requestMessageId = requestMessageId;
	}

	public String getPaymentProduct() {
		return paymentProduct;
	}

	public void setPaymentProduct(String paymentProduct) {
		this.paymentProduct = paymentProduct;
	}

	public String getRecordsCount() {
		return recordsCount;
	}

	public void setRecordsCount(String recordsCount) {
		this.recordsCount = recordsCount;
	}

	public String getCorporateId() {
		return corporateId;
	}

	public void setCorporateId(String corporateId) {
		this.corporateId = corporateId;
	}

	public String getAuthmod() {
		return authmod;
	}

	public void setAuthmod(String authmod) {
		this.authmod = authmod;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getMakerId() {
		return makerId;
	}

	public void setMakerId(String makerId) {
		this.makerId = makerId;
	}

	public String getCheckerId() {
		return checkerId;
	}

	public void setCheckerId(String checkerId) {
		this.checkerId = checkerId;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public boolean isSignaturePresent() {
		return signaturePresent;
	}

	public void setSignaturePresent(boolean signaturePresent) {
		this.signaturePresent = signaturePresent;
	}

	public DscResult getDscResult() {
		return dscResult;
	}

	public void setDscResult(String result) {
		if (result == null) {
			this.dscResult = DscResult.NOT_APPLICABLE;
			return;
		}
		switch (result.trim().toUpperCase()) {
		case "VALID" -> this.dscResult = DscResult.VALID;
		case "INVALID" -> this.dscResult = DscResult.INVALID;
		case "DSCTAGNOTFOUND" -> this.dscResult = DscResult.DSCTAGNOTFOUND;
		default -> this.dscResult = DscResult.NOT_APPLICABLE;
		}
	}

	public void setDscResult(DscResult d) {
		this.dscResult = d == null ? DscResult.NOT_APPLICABLE : d;
	}

	public boolean isFormatValid() {
		return formatValid;
	}

	public void setFormatValid(boolean formatValid) {
		this.formatValid = formatValid;
	}

	public List<ParsedBatchBlockDTO> getBatches() {
		return batches;
	}

	public void setBatches(List<ParsedBatchBlockDTO> batches) {
		this.batches = batches;
	}

	public List<ParsedDebitDTO> getDebits() {
		return debits;
	}

	public void setDebits(List<ParsedDebitDTO> debits) {
		this.debits = debits;
	}

	public List<ErrorDetailDTO> getGlobalErrors() {
		return globalErrors;
	}

	public void setGlobalErrors(List<ErrorDetailDTO> globalErrors) {
		this.globalErrors = globalErrors;
	}

	public String getBatchNumber() {
		return batchNumber;
	}

	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}

	public String getResponseMessageId() {
		return responseMessageId;
	}

	public void setResponseMessageId(String v) {
		this.responseMessageId = v;
	}

	public Long getBatchTime() {
		return batchTime;
	}

	public void setBatchTime(Long batchTime) {
		this.batchTime = batchTime;
	}

	public Integer getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(Integer recordCount) {
		this.recordCount = recordCount;
	}

	public Date getDebitDate() {
		return debitDate;
	}

	public void setDebitDate(Date debitDate) {
		this.debitDate = debitDate;
	}

	public String getAuthMaker() {
		return authMaker;
	}

	public void setAuthMaker(String authMaker) {
		this.authMaker = authMaker;
	}

	public String getAuthChecker() {
		return authChecker;
	}

	public void setAuthChecker(String authChecker) {
		this.authChecker = authChecker;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	

}
