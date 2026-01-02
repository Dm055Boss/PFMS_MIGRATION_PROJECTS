package com.intech.EpayIniFileGenerator.entity;

//src/main/java/com/intech/cpsmsini/entity/PaymentDebitMaster.java

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "PAYMENT_DEBIT_MASTER")
@Getter
@Setter
@NoArgsConstructor
public class PaymentDebitMaster {
	@Id
	@Column(name = "SEQ_DEBIT_ID")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BATCH_ID", nullable = false)
	private PaymentBatchMaster batch;

	@Column(name = "AGENCY_IFSC")
	private String agencyIfsc;
	@Column(name = "AGENCY_ACCOUNT_NUMBER")
	private String agencyAccountNumber;
	@Column(name = "AGENCY_ACCOUNT_NAME")
	private String agencyAccountName;
	@Column(name = "DEBIT_AMOUNT")
	private Double debitAmount;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DEBIT_DATE")
	private Date debitDate;
	@Column(name = "CPSMS_DEBIT_TRAN_ID")
	private String cpsmsDebitTranId;
	@Column(name = "DEBIT_STATUS")
	private String debitStatus;
	@Column(name = "DEBIT_STAN")
	private String debitStan;
	@Column(name = "DEBIT_TRAN_ID")
	private String debitTranId;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DEBIT_TRAN_DATE")
	private Date debitTranDate;
	@Column(name = "SYNC_STATUS")
	private String syncStatus;
	@Column(name = "REMARKS")
	private String remarks;
}
