// domain/entity/CpsmsAudit.java
package com.intech.cpsms.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "TBL_CPSMS_AUDIT_TABLE")
@Getter
@Setter
public class CpsmsAudit {
	@Id
	@SequenceGenerator(name="SEQ_AUD", sequenceName="PFMS.SEQ_AUD", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SEQ_AUD")
	@Column(name = "ID")
	private Long id;

	@Column(name = "REQUESTMESSAGEID")
	private String requestMessageId;
	@Column(name = "RESPONSEMESSAGEID")
	private String responseMessageId;
	@Column(name = "BATCHNUMBER")
	private String batchNumber;
	@Column(name = "CPSMSDEBITTRANID")
	private String cpsmsDebitTranId;
	@Column(name = "CPSMSCREDITTRANID")
	private String cpsmsCreditTranId;
	@Column(name = "BATCHSTATUS")
	private String batchStatus;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_TIME")
	private Date dateTime;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "RESPONSEMESSAGETIME")
	private Date responseMessageTime;
	
//	@Column(name = "ACK_CODE", length = 1)
	@Column(name = "ACK_CODE", columnDefinition = "CHAR(1)")
	private String ackCode;
	
	@Column(name = "ERROR_CODE", length = 100)
	private String errorCode;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ACK_RECEIVED_DATE")
	private Date ackReceivedDate;
	@Column(name = "MAKERID")
	private String makerId;
	@Column(name = "CHECKERID")
	private String checkerId;
	@Column(name = "REMARKS1", length = 200)
	private String remarks1;
	@Column(name = "REMARKS2", length = 200)
	private String remarks2;
	@Column(name = "BANK_ID", length = 8)
	private String bankId;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getBatchNumber() {
		return batchNumber;
	}
	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}
	public String getCpsmsDebitTranId() {
		return cpsmsDebitTranId;
	}
	public void setCpsmsDebitTranId(String cpsmsDebitTranId) {
		this.cpsmsDebitTranId = cpsmsDebitTranId;
	}
	public String getCpsmsCreditTranId() {
		return cpsmsCreditTranId;
	}
	public void setCpsmsCreditTranId(String cpsmsCreditTranId) {
		this.cpsmsCreditTranId = cpsmsCreditTranId;
	}
	public String getBatchStatus() {
		return batchStatus;
	}
	public void setBatchStatus(String batchStatus) {
		this.batchStatus = batchStatus;
	}
	public Date getDateTime() {
		return dateTime;
	}
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	public Date getResponseMessageTime() {
		return responseMessageTime;
	}
	public void setResponseMessageTime(Date responseMessageTime) {
		this.responseMessageTime = responseMessageTime;
	}
	public String getAckCode() {
		return ackCode;
	}
	public void setAckCode(String ackCode) {
		this.ackCode = ackCode;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public Date getAckReceivedDate() {
		return ackReceivedDate;
	}
	public void setAckReceivedDate(Date ackReceivedDate) {
		this.ackReceivedDate = ackReceivedDate;
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
	public String getRemarks1() {
		return remarks1;
	}
	public void setRemarks1(String remarks1) {
		this.remarks1 = remarks1;
	}
	public String getRemarks2() {
		return remarks2;
	}
	public void setRemarks2(String remarks2) {
		this.remarks2 = remarks2;
	}
	public String getBankId() {
		return bankId;
	}
	public void setBankId(String bankId) {
		this.bankId = bankId;
	}
	
	
	
}
