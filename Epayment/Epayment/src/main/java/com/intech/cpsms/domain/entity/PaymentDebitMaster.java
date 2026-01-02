// domain/entity/PaymentDebitMaster.java
package com.intech.cpsms.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "PAYMENT_DEBIT_MASTER")
@Getter
@Setter
public class PaymentDebitMaster {
	@Id
	@SequenceGenerator(name="SEQ_PDM", sequenceName="PFMS.SEQ_PDM", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SEQ_PDM")
	@Column(name = "SEQ_DEBIT_ID")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BATCH_ID", nullable = false)
	private PaymentBatchMaster batch;

	@Column(name = "AGENCY_IFSC", length = 11)
	private String agencyIfsc;
	@Column(name = "AGENCY_ACCOUNT_NUMBER", length = 35)
	private String agencyAccountNumber;
	@Column(name = "AGENCY_ACCOUNT_NAME", length = 50)
	private String agencyAccountName;
	@Column(name = "DEBIT_AMOUNT", precision = 21, scale = 2)
	private java.math.BigDecimal debitAmount;
	@Temporal(TemporalType.DATE)
	@Column(name = "DEBIT_DATE")
	private Date debitDate;
	@Column(name = "CPSMS_DEBIT_TRAN_ID", length = 16)
	private String cpsmsDebitTranId;
	@Column(name = "DEBIT_STATUS", length = 2)
	private String debitStatus;
	@Column(name = "DEBIT_STAN", length = 15)
	private String debitStan;
	@Column(name = "DEBIT_TRAN_ID", length = 9)
	private String debitTranId;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DEBIT_TRAN_DATE")
	private Date debitTranDate;
//	@Column(name = "SYNC_STATUS", length = 1)
//	private String syncStatus; // 'S' or 'E'
	@Column(name = "SYNC_STATUS", columnDefinition = "CHAR(1)")
	private String syncStatus; 
	
	@Column(name = "REMARKS", length = 500)
	private String remarks;

	@OneToMany(mappedBy = "debit", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PaymentCreditMaster> credits;

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

	public String getAgencyIfsc() {
		return agencyIfsc;
	}

	public void setAgencyIfsc(String agencyIfsc) {
		this.agencyIfsc = agencyIfsc;
	}

	public String getAgencyAccountNumber() {
		return agencyAccountNumber;
	}

	public void setAgencyAccountNumber(String agencyAccountNumber) {
		this.agencyAccountNumber = agencyAccountNumber;
	}

	public String getAgencyAccountName() {
		return agencyAccountName;
	}

	public void setAgencyAccountName(String agencyAccountName) {
		this.agencyAccountName = agencyAccountName;
	}

	public java.math.BigDecimal getDebitAmount() {
		return debitAmount;
	}

	public void setDebitAmount(java.math.BigDecimal debitAmount) {
		this.debitAmount = debitAmount;
	}

	public Date getDebitDate() {
		return debitDate;
	}

	public void setDebitDate(Date debitDate) {
		this.debitDate = debitDate;
	}

	public String getCpsmsDebitTranId() {
		return cpsmsDebitTranId;
	}

	public void setCpsmsDebitTranId(String cpsmsDebitTranId) {
		this.cpsmsDebitTranId = cpsmsDebitTranId;
	}

	public String getDebitStatus() {
		return debitStatus;
	}

	public void setDebitStatus(String debitStatus) {
		this.debitStatus = debitStatus;
	}

	public String getDebitStan() {
		return debitStan;
	}

	public void setDebitStan(String debitStan) {
		this.debitStan = debitStan;
	}

	public String getDebitTranId() {
		return debitTranId;
	}

	public void setDebitTranId(String debitTranId) {
		this.debitTranId = debitTranId;
	}

	public Date getDebitTranDate() {
		return debitTranDate;
	}

	public void setDebitTranDate(Date debitTranDate) {
		this.debitTranDate = debitTranDate;
	}

	public String getSyncStatus() {
		return syncStatus;
	}

	public void setSyncStatus(String syncStatus) {
		this.syncStatus = syncStatus;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public List<PaymentCreditMaster> getCredits() {
		return credits;
	}

	public void setCredits(List<PaymentCreditMaster> credits) {
		this.credits = credits;
	}
	
	
}
