// src/main/java/com/intech/cpsms/dto/ErrorDetailDTO.java
package com.intech.cpsms.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorDetailDTO {
	private String errorCode; // e.g., N001, N002, ...
	private String errorMessage; // remarks text (optional)
	private String batchNumber; // optional
	private String creditTranId; // optional (CPSMS_CREDIT_TRAN), if per-record error
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getBatchNumber() {
		return batchNumber;
	}
	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}
	public String getCreditTranId() {
		return creditTranId;
	}
	public void setCreditTranId(String creditTranId) {
		this.creditTranId = creditTranId;
	}
	
	
}
