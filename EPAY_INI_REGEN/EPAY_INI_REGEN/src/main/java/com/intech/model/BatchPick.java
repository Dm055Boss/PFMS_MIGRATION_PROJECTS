package com.intech.model;

//src/main/java/com/intech/cpsmsini/model/BatchPick.java

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchPick {
	private Long batchId; // PBM.SEQ_BATCH_ID
	private String batchNumber; // PBM.BATCH_NUMBER
	private String product; // SUBSTR(PBM.REQUEST_MESSAGE_ID, 4, 3)
	private String bankCode; // SUBSTR(PBM.REQUEST_MESSAGE_ID, 1, 3)
	public Long getBatchId() {
		return batchId;
	}
	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}
	public String getBatchNumber() {
		return batchNumber;
	}
	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getBankCode() {
		return bankCode;
	}
	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	public BatchPick(Long batchId, String batchNumber, String product, String bankCode) {
		super();
		this.batchId = batchId;
		this.batchNumber = batchNumber;
		this.product = product;
		this.bankCode = bankCode;
	}
	
	
	
}
