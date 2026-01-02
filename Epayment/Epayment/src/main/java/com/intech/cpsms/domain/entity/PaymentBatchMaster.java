// domain/entity/PaymentBatchMaster.java
package com.intech.cpsms.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "PAYMENT_BATCH_MASTER")
@Getter
@Setter
public class PaymentBatchMaster {
	@Id
	@Column(name = "SEQ_BATCH_ID")
	@SequenceGenerator(name = "SEQ_PBM", sequenceName = "PFMS.SEQ_PBM", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PBM")
	private Long id; // Use your sequence outside, or switch to IDENTITY if DB supports

	@Column(name = "CORPORATE_ID")
	private String corporateId;
	@Column(name = "BATCH_NUMBER", length = 16)
	private String batchNumber;
	@Column(name = "BATCH_TIME")
	private Long batchTime;
	@Column(name = "RECORD_COUNT")
	private Integer recordCount;
	@Column(name = "PAYMENT_PROD")
	private String paymentProd;
	@Column(name = "AUTHMOD")
	private String authmod;
	@Column(name = "REQUEST_MESSAGE_ID")
	private String requestMessageId;
	@Column(name = "RESPONSE_MESSAGE_ID")
	private String responseMessageId;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "MODIFIED_DATE")
	private Date modifiedDate;
	@Column(name = "MAKER_ID")
	private String makerId;
	@Column(name = "CHECKER_ID")
	private String checkerId;
	@Column(name = "REMARKS", length = 500)
	private String remarks;
	@Temporal(TemporalType.DATE)
	@Column(name = "DEBIT_DATE")
	private Date debitDate;
	@Column(name = "AUTH_MAKER")
	private String authMaker;
	@Column(name = "AUTH_CHECKER")
	private String authChecker;

	@OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PaymentDebitMaster> debits;

	@OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PaymentCreditMaster> credits;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCorporateId() {
		return corporateId;
	}

	public void setCorporateId(String corporateId) {
		this.corporateId = corporateId;
	}

	public String getBatchNumber() {
		return batchNumber;
	}

	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
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

	public String getPaymentProd() {
		return paymentProd;
	}

	public void setPaymentProd(String paymentProd) {
		this.paymentProd = paymentProd;
	}

	public String getAuthmod() {
		return authmod;
	}

	public void setAuthmod(String authmod) {
		this.authmod = authmod;
	}

	public String getRequestMessageId() {
		return requestMessageId;
	}

	public void setRequestMessageId(String requestMessageId) {
		this.requestMessageId = requestMessageId;
	}

	public String getResponseMessageId() {
		return responseMessageId;
	}

	public void setResponseMessageId(String responseMessageId) {
		this.responseMessageId = responseMessageId;
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

	public List<PaymentDebitMaster> getDebits() {
		return debits;
	}

	public void setDebits(List<PaymentDebitMaster> debits) {
		this.debits = debits;
	}

	public List<PaymentCreditMaster> getCredits() {
		return credits;
	}

	public void setCredits(List<PaymentCreditMaster> credits) {
		this.credits = credits;
	}
	
	
	
	
}
