// src/main/java/com/intech/cpsms/dto/ParsedCreditDTO.java
package com.intech.cpsms.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedCreditDTO {
	// FK context (resolved during persist)
	private Long batchId; // to set later
	private Long debitId; // to set later

	// From XML
	private BigDecimal creditAmount;
	private String creditIfsc;
	private String creditAccountNumber;
	private String creditAccountName;
	private String creditAccountAddress;
	private Long creditUid; // NUMBER(12,0)
	private String creditBankIin;

	private String pmtMtd; // PMT_MTD
	private String rmtInf; // RMT_INF

	private String cpsmsCreditTran; // CPSMS_CREDIT_TRAN
	private String initiatingUtr;
	private String newInitiatingUtr;
	private String returnedUtr;
	private String tranIdFt;
	private Date tranDateFt;

	private String creditStatus; // CREDIT_STATUS
	private String creditStan; // CREDIT_STAN

	// Persistence-time fields
	private String payRejectReason; // PAY_REJECT_REASON
	private String creReturnReason; // CRE_RETURN_REASON
	private String paymentInitiReqId;
	private String paymentSuccessReqId;
	private String failureRequestId;

	private Date createdDate;
	private Date modifiedDate;
	private String checksum;
	private String remarks;
	private String leiCode;

	private String c6346Ini;
	private String c6366Ini;
	private String c6346Succ;
	private String c6366Succ;
	private String c6346Rej;
	private String c6366Rej;

	private String makerLei;
	private String checkerLei;

	// Derived at persist time
	private SyncStatus syncStatus; // 'S' or 'E' from DSC + format

	// For error mapping/reporting
	private ErrorDetailDTO error; // optional, if this record has an error

	public Long getBatchId() {
		return batchId;
	}

	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}

	public Long getDebitId() {
		return debitId;
	}

	public void setDebitId(Long debitId) {
		this.debitId = debitId;
	}

	public BigDecimal getCreditAmount() {
		return creditAmount;
	}

	public void setCreditAmount(BigDecimal creditAmount) {
		this.creditAmount = creditAmount;
	}

	public String getCreditIfsc() {
		return creditIfsc;
	}

	public void setCreditIfsc(String creditIfsc) {
		this.creditIfsc = creditIfsc;
	}

	public String getCreditAccountNumber() {
		return creditAccountNumber;
	}

	public void setCreditAccountNumber(String creditAccountNumber) {
		this.creditAccountNumber = creditAccountNumber;
	}

	public String getCreditAccountName() {
		return creditAccountName;
	}

	public void setCreditAccountName(String creditAccountName) {
		this.creditAccountName = creditAccountName;
	}

	public String getCreditAccountAddress() {
		return creditAccountAddress;
	}

	public void setCreditAccountAddress(String creditAccountAddress) {
		this.creditAccountAddress = creditAccountAddress;
	}

	public Long getCreditUid() {
		return creditUid;
	}

	public void setCreditUid(Long creditUid) {
		this.creditUid = creditUid;
	}

	public String getCreditBankIin() {
		return creditBankIin;
	}

	public void setCreditBankIin(String creditBankIin) {
		this.creditBankIin = creditBankIin;
	}

	public String getPmtMtd() {
		return pmtMtd;
	}

	public void setPmtMtd(String pmtMtd) {
		this.pmtMtd = pmtMtd;
	}

	public String getRmtInf() {
		return rmtInf;
	}

	public void setRmtInf(String rmtInf) {
		this.rmtInf = rmtInf;
	}

	public String getCpsmsCreditTran() {
		return cpsmsCreditTran;
	}

	public void setCpsmsCreditTran(String cpsmsCreditTran) {
		this.cpsmsCreditTran = cpsmsCreditTran;
	}

	public String getInitiatingUtr() {
		return initiatingUtr;
	}

	public void setInitiatingUtr(String initiatingUtr) {
		this.initiatingUtr = initiatingUtr;
	}

	public String getNewInitiatingUtr() {
		return newInitiatingUtr;
	}

	public void setNewInitiatingUtr(String newInitiatingUtr) {
		this.newInitiatingUtr = newInitiatingUtr;
	}

	public String getReturnedUtr() {
		return returnedUtr;
	}

	public void setReturnedUtr(String returnedUtr) {
		this.returnedUtr = returnedUtr;
	}

	public String getTranIdFt() {
		return tranIdFt;
	}

	public void setTranIdFt(String tranIdFt) {
		this.tranIdFt = tranIdFt;
	}

	public Date getTranDateFt() {
		return tranDateFt;
	}

	public void setTranDateFt(Date tranDateFt) {
		this.tranDateFt = tranDateFt;
	}

	public String getCreditStatus() {
		return creditStatus;
	}

	public void setCreditStatus(String creditStatus) {
		this.creditStatus = creditStatus;
	}

	public String getCreditStan() {
		return creditStan;
	}

	public void setCreditStan(String creditStan) {
		this.creditStan = creditStan;
	}

	public String getPayRejectReason() {
		return payRejectReason;
	}

	public void setPayRejectReason(String payRejectReason) {
		this.payRejectReason = payRejectReason;
	}

	public String getCreReturnReason() {
		return creReturnReason;
	}

	public void setCreReturnReason(String creReturnReason) {
		this.creReturnReason = creReturnReason;
	}

	public String getPaymentInitiReqId() {
		return paymentInitiReqId;
	}

	public void setPaymentInitiReqId(String paymentInitiReqId) {
		this.paymentInitiReqId = paymentInitiReqId;
	}

	public String getPaymentSuccessReqId() {
		return paymentSuccessReqId;
	}

	public void setPaymentSuccessReqId(String paymentSuccessReqId) {
		this.paymentSuccessReqId = paymentSuccessReqId;
	}

	public String getFailureRequestId() {
		return failureRequestId;
	}

	public void setFailureRequestId(String failureRequestId) {
		this.failureRequestId = failureRequestId;
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

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getLeiCode() {
		return leiCode;
	}

	public void setLeiCode(String leiCode) {
		this.leiCode = leiCode;
	}

	public String getC6346Ini() {
		return c6346Ini;
	}

	public void setC6346Ini(String c6346Ini) {
		this.c6346Ini = c6346Ini;
	}

	public String getC6366Ini() {
		return c6366Ini;
	}

	public void setC6366Ini(String c6366Ini) {
		this.c6366Ini = c6366Ini;
	}

	public String getC6346Succ() {
		return c6346Succ;
	}

	public void setC6346Succ(String c6346Succ) {
		this.c6346Succ = c6346Succ;
	}

	public String getC6366Succ() {
		return c6366Succ;
	}

	public void setC6366Succ(String c6366Succ) {
		this.c6366Succ = c6366Succ;
	}

	public String getC6346Rej() {
		return c6346Rej;
	}

	public void setC6346Rej(String c6346Rej) {
		this.c6346Rej = c6346Rej;
	}

	public String getC6366Rej() {
		return c6366Rej;
	}

	public void setC6366Rej(String c6366Rej) {
		this.c6366Rej = c6366Rej;
	}

	public String getMakerLei() {
		return makerLei;
	}

	public void setMakerLei(String makerLei) {
		this.makerLei = makerLei;
	}

	public String getCheckerLei() {
		return checkerLei;
	}

	public void setCheckerLei(String checkerLei) {
		this.checkerLei = checkerLei;
	}

	public SyncStatus getSyncStatus() {
		return syncStatus;
	}

	public void setSyncStatus(SyncStatus syncStatus) {
		this.syncStatus = syncStatus;
	}

	public ErrorDetailDTO getError() {
		return error;
	}

	public void setError(ErrorDetailDTO error) {
		this.error = error;
	}
	
	
}
