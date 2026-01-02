// src/main/java/com/intech/cpsms/dto/ParsedDebitDTO.java
package com.intech.cpsms.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedDebitDTO {
  // FK context (resolved during persist)
  private Long batchId;               // to set later

  // From XML debit header
  private String agencyIfsc;
  private String agencyAccountNumber;
  private String agencyAccountName;
  private BigDecimal debitAmount;
  private Date   debitDate;
  private String cpsmsDebitTranId;
  private String debitStatus;
  private String debitStan;
  private String debitTranId;
  private Date   debitTranDate;

  // Derived at persist time
  private SyncStatus syncStatus;      // 'S' or 'E' from DSC + format
  private String remarks;

  // Nested credits belonging to this debit
  private List<ParsedCreditDTO> credits;

  // For error mapping/reporting
  private ErrorDetailDTO error;       // optional, if the debit-level has an error

  public Long getBatchId() {
	return batchId;
  }

  public void setBatchId(Long batchId) {
	this.batchId = batchId;
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

  public BigDecimal getDebitAmount() {
	return debitAmount;
  }

  public void setDebitAmount(BigDecimal debitAmount) {
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

  public SyncStatus getSyncStatus() {
	return syncStatus;
  }

  public void setSyncStatus(SyncStatus syncStatus) {
	this.syncStatus = syncStatus;
  }

  public String getRemarks() {
	return remarks;
  }

  public void setRemarks(String remarks) {
	this.remarks = remarks;
  }

  public List<ParsedCreditDTO> getCredits() {
	return credits;
  }

  public void setCredits(List<ParsedCreditDTO> credits) {
	this.credits = credits;
  }

  public ErrorDetailDTO getError() {
	return error;
  }

  public void setError(ErrorDetailDTO error) {
	this.error = error;
  }
  
  
}
