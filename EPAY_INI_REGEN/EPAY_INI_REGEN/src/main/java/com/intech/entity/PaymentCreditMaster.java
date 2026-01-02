package com.intech.entity;

import java.util.Date;

//src/main/java/com/intech/cpsmsini/entity/PaymentCreditMaster.java
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="PAYMENT_CREDIT_MASTER")
@Getter @Setter @NoArgsConstructor
public class PaymentCreditMaster {
@Id @Column(name="SEQ_CREDIT_ID") private Long id;

@ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="SEQ_BATCH_ID")
private PaymentBatchMaster batch;

@ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="DEBIT_ID")
private PaymentDebitMaster debit;

@Column(name="CREDIT_AMOUNT")         private Double creditAmount;
@Column(name="CREDIT_IFSC")           private String creditIfsc;
@Column(name="CREDIT_ACCOUNT_NUMBER") private String creditAccountNumber;
@Column(name="CREDIT_ACCOUNT_NAME")   private String creditAccountName;
@Column(name="CREDIT_ACCOUNT_ADDRESS") private String creditAccountAddress;
@Column(name="CREDIT_UID")            private Long creditUid;
@Column(name="CREDIT_BANK_IIN")       private String creditBankIin;

@Column(name="PMT_MTD")               private String pmtMtd;
@Column(name="RMT_INF")               private String rmtInf;

@Column(name="CPSMS_CREDIT_TRAN")     private String cpsmsCreditTran;
@Column(name="INITIATING_UTR")        private String initiatingUtr;
@Column(name="NEW_INITIATING_UTR")    private String newInitiatingUtr;
@Column(name="RETURNED_UTR")          private String returnedUtr;
@Column(name="TRAN_ID_FT")            private String tranIdFt;
@Temporal(TemporalType.TIMESTAMP) @Column(name="TRAN_DATE_FT") private Date tranDateFt;

@Column(name="CREDIT_STATUS")         private String creditStatus;
@Column(name="CREDIT_STAN")           private String creditStan;
@Column(name="SYNC_STATUS")           private String syncStatus;

@Column(name="PAY_REJECT_REASON")     private String payRejectReason;
@Column(name="CRE_RETURN_REASON")     private String creReturnReason;

@Column(name="PAYMENT_INITI_REQ_ID")  private String paymentInitiReqId;
@Column(name="PAYMENT_SUCCESS_REQ_ID") private String paymentSuccessReqId;
@Column(name="FAILURE_REQUEST_ID")    private String failureRequestId;

@Temporal(TemporalType.TIMESTAMP) @Column(name="CREATED_DATE")  private Date createdDate;
@Temporal(TemporalType.TIMESTAMP) @Column(name="MODIFIED_DATE") private Date modifiedDate;

@Column(name="CHECKSUM")              private String checksum;
@Column(name="REMARKS")               private String remarks;
@Column(name="LEI_CODE")              private String leiCode;

// INI code columns (if present in data)
@Column(name="C6346_INI") private String c6346Ini;
@Column(name="C6366_INI") private String c6366Ini;
@Column(name="C6346_SUCC") private String c6346Succ;
@Column(name="C6366_SUCC") private String c6366Succ;
@Column(name="C6346_REJ") private String c6346Rej;
@Column(name="C6366_REJ") private String c6366Rej;

@Column(name="Maker_LEI")   private String makerLei;
@Column(name="Checker_LEI") private String checkerLei;
}
