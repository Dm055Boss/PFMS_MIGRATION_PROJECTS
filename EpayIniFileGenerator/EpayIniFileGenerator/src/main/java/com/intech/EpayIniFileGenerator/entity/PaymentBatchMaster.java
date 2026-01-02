package com.intech.EpayIniFileGenerator.entity;

//src/main/java/com/intech/cpsmsini/entity/PaymentBatchMaster.java

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "PAYMENT_BATCH_MASTER")
@Getter
@Setter
@NoArgsConstructor
public class PaymentBatchMaster {
	@Id
	@Column(name = "SEQ_BATCH_ID")
	private Long id;

	@Column(name = "CORPORATE_ID")
	private String corporateId;
	@Column(name = "BATCH_NUMBER")
	private String batchNumber;
	@Column(name = "BATCH_TIME")
	private Long batchTime;
	@Column(name = "RECORD_COUNT")
	private Long recordCount;
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
	@Column(name = "REMARKS")
	private String remarks;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DEBIT_DATE")
	private Date debitDate;
	@Column(name = "AUTH_MAKER")
	private String authMaker;
	@Column(name = "AUTH_CHECKER")
	private String authChecker;

	@OneToMany(mappedBy = "batch", fetch = FetchType.LAZY)
	private List<PaymentDebitMaster> debits;
	@OneToMany(mappedBy = "batch", fetch = FetchType.LAZY)
	private List<PaymentCreditMaster> credits;
}
