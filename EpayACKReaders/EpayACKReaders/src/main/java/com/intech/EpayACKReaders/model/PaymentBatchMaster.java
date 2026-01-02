// src/main/java/com/intech/epayackreader/model/PaymentBatchMaster.java
package com.intech.EpayACKReaders.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "PAYMENT_BATCH_MASTER")
public class PaymentBatchMaster {


    @Id
    @Column(name = "SEQ_BATCH_ID")
    private Long id;

    @Column(name = "BATCH_NUMBER")
    private String batchNumber;

    @Column(name = "REQUEST_MESSAGE_ID")
    private String requestMessageId;

    // ==== Other columns are omitted for brevity ====
    // Add them if you want a full mapping.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getRequestMessageId() {
        return requestMessageId;
    }

    public void setRequestMessageId(String requestMessageId) {
        this.requestMessageId = requestMessageId;
    }
}
