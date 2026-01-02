package com.intech.model;

/**
 * Represents a batch that needs a Reject XML file.
 */
public class BatchInfo {

    private Long batchId;
    private String batchNumber;
    private String paymentProduct;
    private String bankCode;

    public BatchInfo(Long batchId, String batchNumber, String paymentProduct, String bankCode) {
        this.batchId = batchId;
        this.batchNumber = batchNumber;
        this.paymentProduct = paymentProduct;
        this.bankCode = bankCode;
    }

    public Long getBatchId() {
        return batchId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public String getPaymentProduct() {
        return paymentProduct;
    }

    public String getBankCode() {
        return bankCode;
    }

    @Override
    public String toString() {
        return "BatchInfo{" +
                "batchId=" + batchId +
                ", batchNumber='" + batchNumber + '\'' +
                ", paymentProduct='" + paymentProduct + '\'' +
                ", bankCode='" + bankCode + '\'' +
                '}';
    }
}
