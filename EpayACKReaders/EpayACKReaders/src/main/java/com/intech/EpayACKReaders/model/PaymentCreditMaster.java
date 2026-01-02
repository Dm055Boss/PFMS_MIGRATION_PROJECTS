// src/main/java/com/intech/epayackreader/model/PaymentCreditMaster.java
package com.intech.EpayACKReaders.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "PAYMENT_CREDIT_MASTER")
public class PaymentCreditMaster {

	@Id
	@Column(name = "SEQ_CREDIT_ID")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SEQ_BATCH_ID")
	private PaymentBatchMaster batch;

	@Column(name = "CPSMS_CREDIT_TRAN")
	private String cpsmsCreditTran;

	// ===== Request file name columns (original filenames) =====

	@Column(name = "PAYMENT_INITI_REQ_ID")
	private String paymentInitiReqId;

	@Column(name = "PAYMENT_SUCCESS_REQ_ID")
	private String paymentSuccessReqId;

	@Column(name = "FAILURE_REQUEST_ID")
	private String failureRequestId;

	// ===== ACK-related columns =====

	@Column(name = "INIACK_NAME")
	private String iniAckName;

	@Column(name = "SUCACK_NAME")
	private String sucAckName;

	@Column(name = "REJACK_NAME")
	private String rejAckName;

	@Column(name = "INI_GRP_STATUS")
	private String iniGrpStatus;

	@Column(name = "SUC_GRP_STATUS")
	private String sucGrpStatus;

	@Column(name = "REJ_GRP_STATUS")
	private String rejGrpStatus;

	@Column(name = "INIACK_ERRORCODE")
	private String iniAckErrorCode;

	@Column(name = "SUCACK_ERRORCODE")
	private String sucAckErrorCode;

	@Column(name = "REJACK_ERRORCODE")
	private String rejAckErrorCode;

	// ===== getters & setters =====

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

	public String getIniAckName() {
		return iniAckName;
	}

	public void setIniAckName(String iniAckName) {
		this.iniAckName = iniAckName;
	}

	public String getSucAckName() {
		return sucAckName;
	}

	public void setSucAckName(String sucAckName) {
		this.sucAckName = sucAckName;
	}

	public String getRejAckName() {
		return rejAckName;
	}

	public void setRejAckName(String rejAckName) {
		this.rejAckName = rejAckName;
	}

	public String getIniGrpStatus() {
		return iniGrpStatus;
	}

	public void setIniGrpStatus(String iniGrpStatus) {
		this.iniGrpStatus = iniGrpStatus;
	}

	public String getSucGrpStatus() {
		return sucGrpStatus;
	}

	public void setSucGrpStatus(String sucGrpStatus) {
		this.sucGrpStatus = sucGrpStatus;
	}

	public String getRejGrpStatus() {
		return rejGrpStatus;
	}

	public void setRejGrpStatus(String rejGrpStatus) {
		this.rejGrpStatus = rejGrpStatus;
	}

	public String getIniAckErrorCode() {
		return iniAckErrorCode;
	}

	public void setIniAckErrorCode(String iniAckErrorCode) {
		this.iniAckErrorCode = iniAckErrorCode;
	}

	public String getSucAckErrorCode() {
		return sucAckErrorCode;
	}

	public void setSucAckErrorCode(String sucAckErrorCode) {
		this.sucAckErrorCode = sucAckErrorCode;
	}

	public String getRejAckErrorCode() {
		return rejAckErrorCode;
	}

	public void setRejAckErrorCode(String rejAckErrorCode) {
		this.rejAckErrorCode = rejAckErrorCode;
	}

	public String getCpsmsCreditTran() {
		return cpsmsCreditTran;
	}

	public void setCpsmsCreditTran(String cpsmsCreditTran) {
		this.cpsmsCreditTran = cpsmsCreditTran;
	}

}
