package com.idbi.cpsms.succack.service.dto;

/**
 * Simple DTO for batch data used in Success file generation.
 */
public class BatchInfo {

    private Long batchId;
    private String batchNumber;
    private String paymentProd;
    private String bankCode;
    private int recordsCount;

    public BatchInfo(Long batchId, String batchNumber, String paymentProd, String bankCode,int recordsCount ) {
        this.batchId = batchId;
        this.batchNumber = batchNumber;
        this.paymentProd = paymentProd;
        this.bankCode = bankCode;
        this.recordsCount=recordsCount;
    }

    public Long getBatchId() {
        return batchId;
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
    
    

    public int getRecordsCount() {
		return recordsCount;
	}

	@Override
	public String toString() {
		return "BatchInfo [batchId=" + batchId + ", batchNumber=" + batchNumber + ", paymentProd=" + paymentProd
				+ ", bankCode=" + bankCode + ", recordsCount=" + recordsCount + "]";
	}
	
	
}
	