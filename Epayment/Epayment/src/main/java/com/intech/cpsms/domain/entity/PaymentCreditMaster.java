// domain/entity/PaymentCreditMaster.java
package com.intech.cpsms.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.math.BigDecimal;

@Entity
@Table(name = "PAYMENT_CREDIT_MASTER")
@Getter
@Setter
public class PaymentCreditMaster {
	@Id
	@SequenceGenerator(name = "SEQ_PCM", sequenceName = "PFMS.SEQ_PCM", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PCM")
	@Column(name = "SEQ_CREDIT_ID")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SEQ_BATCH_ID")
	private PaymentBatchMaster batch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DEBIT_ID")
	private PaymentDebitMaster debit;

	@Column(name = "CREDIT_AMOUNT", precision = 21, scale = 2)
	private BigDecimal creditAmount;
	@Column(name = "CREDIT_IFSC", length = 11)
	private String creditIfsc;
	@Column(name = "CREDIT_ACCOUNT_NUMBER", length = 35)
	private String creditAccountNumber;
	@Column(name = "CREDIT_ACCOUNT_NAME", length = 50)
	private String creditAccountName;
	@Column(name = "CREDIT_ACCOUNT_ADDRESS", length = 140)
	private String creditAccountAddress;
	@Column(name = "CREDIT_UID", precision = 12, scale = 0)
	private Long creditUid;
	@Column(name = "CREDIT_BANK_IIN", length = 9)
	private String creditBankIin;

	@Column(name = "PMT_MTD", length = 4)
	private String pmtMtd;
	@Column(name = "RMT_INF", length = 150)
	private String rmtInf;

	@Column(name = "CPSMS_CREDIT_TRAN", length = 16)
	private String cpsmsCreditTran;
	@Column(name = "INITIATING_UTR", length = 35)
	private String initiatingUtr;
	@Column(name = "NEW_INITIATING_UTR", length = 35)
	private String newInitiatingUtr;
	@Column(name = "RETURNED_UTR", length = 35)
	private String returnedUtr;
	@Column(name = "TRAN_ID_FT", length = 35)
	private String tranIdFt;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TRAN_DATE_FT")
	private Date tranDateFt;

	@Column(name = "CREDIT_STATUS", length = 2)
	private String creditStatus;
	@Column(name = "CREDIT_STAN", length = 15)
	private String creditStan;

//	@Column(name = "SYNC_STATUS", length = 1)
//	private String syncStatus; // 'S' or 'E'
	@Column(name = "SYNC_STATUS", columnDefinition = "CHAR(1)")
	private String syncStatus;

	@Column(name = "PAY_REJECT_REASON", length = 100)
	private String payRejectReason;
	@Column(name = "CRE_RETURN_REASON", length = 100)
	private String creReturnReason;
	@Column(name = "PAYMENT_INITI_REQ_ID", length = 50)
	private String paymentInitiReqId;
	@Column(name = "PAYMENT_SUCCESS_REQ_ID", length = 50)
	private String paymentSuccessReqId;
	@Column(name = "FAILURE_REQUEST_ID", length = 100)
	private String failureRequestId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "MODIFIED_DATE")
	private Date modifiedDate;
	@Column(name = "CHECKSUM", length = 40)
	private String checksum;
	@Column(name = "REMARKS", length = 500)
	private String remarks;
	@Column(name = "LEI_CODE", length = 35)
	private String leiCode;

	@Column(name = "C6346_INI", length = 5)
	private String c6346Ini;
	@Column(name = "C6366_INI", length = 5)
	private String c6366Ini;
	@Column(name = "C6346_SUCC", length = 5)
	private String c6346Succ;
	@Column(name = "C6366_SUCC", length = 5)
	private String c6366Succ;
	@Column(name = "C6346_REJ", length = 5)
	private String c6346Rej;
	@Column(name = "C6366_REJ", length = 5)
	private String c6366Rej;

	@Column(name = "Maker_LEI", length = 10)
	private String makerLei;
	@Column(name = "Checker_LEI", length = 10)
	private String checkerLei;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PaymentBatchMaster getBatch() {
		return batch;
	}

	public void setBatch(PaymentBatchMaster batch) {
		this.batch = batch;
	}

	public PaymentDebitMaster getDebit() {
		return debit;
	}

	public void setDebit(PaymentDebitMaster debit) {
		this.debit = debit;
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

	public String getSyncStatus() {
		return syncStatus;
	}

	public void setSyncStatus(String syncStatus) {
		this.syncStatus = syncStatus;
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

}
