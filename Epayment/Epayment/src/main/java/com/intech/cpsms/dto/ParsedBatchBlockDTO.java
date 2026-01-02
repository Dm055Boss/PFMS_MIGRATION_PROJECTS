// src/main/java/com/intech/cpsms/dto/ParsedBatchBlockDTO.java
package com.intech.cpsms.dto;

import lombok.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedBatchBlockDTO {
	// Batch-level info from XML
	private String batchNumber; // BATCH_NUMBER
	private Long batchTime; // BATCH_TIME (if present as numeric)
	private Integer recordCount; // per batch if needed (optional)
	private Date debitDate; // if provided at batch-level

	// For error mapping/reporting
	private String batchStatus; // optional status code to reflect into audit
	private ErrorDetailDTO error; // optional
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
	public Date getDebitDate() {
		return debitDate;
	}
	public void setDebitDate(Date debitDate) {
		this.debitDate = debitDate;
	}
	public String getBatchStatus() {
		return batchStatus;
	}
	public void setBatchStatus(String batchStatus) {
		this.batchStatus = batchStatus;
	}
	public ErrorDetailDTO getError() {
		return error;
	}
	public void setError(ErrorDetailDTO error) {
		this.error = error;
	}
	
	
}
