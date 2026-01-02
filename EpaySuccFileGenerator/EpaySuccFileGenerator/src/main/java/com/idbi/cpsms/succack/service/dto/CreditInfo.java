package com.idbi.cpsms.succack.service.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * DTO for credit details used in Success file generation.
 */
public class CreditInfo {

    private Long id;
    private String batchNumber;
    private String paymentProd;
    private String bankCode;
    private String cpsmsCreditTran;
    private String creditIfsc;
    private String creditAccountNumber;
    private String creditBankIin;
    private String creditUid;
    private String creditAccountName;
    private BigDecimal creditAmount;
    private String creditStatus;
    private String pmtMtd;
    private Timestamp tranDateFt;
    private String tranIdFt;
    private String initiatingUtr;
    private String debitTranId;

    public CreditInfo(Long id,
                      String batchNumber,
                      String paymentProd,
                      String bankCode,
                      String cpsmsCreditTran,
                      String creditIfsc,
                      String creditAccountNumber,
                      String creditBankIin,
                      String creditUid,
                      String creditAccountName,
                      BigDecimal creditAmount,
                      String creditStatus,
                      String pmtMtd,
                      Timestamp tranDateFt,
                      String tranIdFt,
                      String initiatingUtr,
                      String debitTranId) {
        this.id = id;
        this.batchNumber = batchNumber;
        this.paymentProd = paymentProd;
        this.bankCode = bankCode;
        this.cpsmsCreditTran = cpsmsCreditTran;
        this.creditIfsc = creditIfsc;
        this.creditAccountNumber = creditAccountNumber;
        this.creditBankIin = creditBankIin;
        this.creditUid = creditUid;
        this.creditAccountName = creditAccountName;
        this.creditAmount = creditAmount;
        this.creditStatus = creditStatus;
        this.pmtMtd = pmtMtd;
        this.tranDateFt = tranDateFt;
        this.tranIdFt = tranIdFt;
        this.initiatingUtr = initiatingUtr;
        this.debitTranId = debitTranId;
    }

    public Long getId() {
        return id;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public String getPaymentProd() {
        return paymentProd;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getCpsmsCreditTran() {
        return cpsmsCreditTran;
    }

    public String getCreditIfsc() {
        return creditIfsc;
    }

    public String getCreditAccountNumber() {
        return creditAccountNumber;
    }

    public String getCreditBankIin() {
        return creditBankIin;
    }

    public String getCreditUid() {
        return creditUid;
    }

    public String getCreditAccountName() {
        return creditAccountName;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public String getCreditStatus() {
        return creditStatus;
    }

    public String getPmtMtd() {
        return pmtMtd;
    }

    public Timestamp getTranDateFt() {
        return tranDateFt;
    }

    public String getTranIdFt() {
        return tranIdFt;
    }

    public String getInitiatingUtr() {
        return initiatingUtr;
    }

    public String getDebitTranId() {
        return debitTranId;
    }

    @Override
    public String toString() {
        return "CreditInfo{" +
                "id=" + id +
                ", batchNumber='" + batchNumber + '\'' +
                ", paymentProd='" + paymentProd + '\'' +
                ", bankCode='" + bankCode + '\'' +
                ", cpsmsCreditTran='" + cpsmsCreditTran + '\'' +
                '}';
    }
}
