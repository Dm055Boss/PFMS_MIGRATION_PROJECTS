package com.intech.domain;

import java.math.BigDecimal;

/**
 * Output of TXN query (one transaction row).
 */
public class TransactionDetail {

    private final String tranDateDdMonYyyy;
    private final String partTranType;
    private final String tranSubType;
    private final String tranParticular;
    private final String instrumentNo;
    private final String instrumentDateDdMmYyyy;
    private final BigDecimal amount;
    private final String remarks;
    private final String tranId;
    private final String partTranSrlNum;
    private final String tranParticular2;
    private final String valueDateDdMmYyyy;

    public TransactionDetail(String tranDateDdMonYyyy,
                             String partTranType,
                             String tranSubType,
                             String tranParticular,
                             String instrumentNo,
                             String instrumentDateDdMmYyyy,
                             BigDecimal amount,
                             String remarks,
                             String tranId,
                             String partTranSrlNum,
                             String tranParticular2,
                             String valueDateDdMmYyyy) {
        this.tranDateDdMonYyyy = tranDateDdMonYyyy;
        this.partTranType = partTranType;
        this.tranSubType = tranSubType;
        this.tranParticular = tranParticular;
        this.instrumentNo = instrumentNo;
        this.instrumentDateDdMmYyyy = instrumentDateDdMmYyyy;
        this.amount = amount;
        this.remarks = remarks;
        this.tranId = tranId;
        this.partTranSrlNum = partTranSrlNum;
        this.tranParticular2 = tranParticular2;
        this.valueDateDdMmYyyy = valueDateDdMmYyyy;
    }

    public String getTranDateDdMonYyyy() { return tranDateDdMonYyyy; }

    public String getPartTranType() { return partTranType; }

    public String getTranSubType() { return tranSubType; }

    public String getTranParticular() { return tranParticular; }

    public String getInstrumentNo() { return instrumentNo; }

    public String getInstrumentDateDdMmYyyy() { return instrumentDateDdMmYyyy; }

    public BigDecimal getAmount() { return amount; }

    public String getRemarks() { return remarks; }

    public String getTranId() { return tranId; }

    public String getPartTranSrlNum() { return partTranSrlNum; }

    public String getTranParticular2() { return tranParticular2; }

    public String getValueDateDdMmYyyy() { return valueDateDdMmYyyy; }
}
