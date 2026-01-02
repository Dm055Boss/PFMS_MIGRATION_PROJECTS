package com.intech.domain;

import java.math.BigDecimal;

/**
 * Output of TXN query (one transaction row).
 */
public class TransactionDetail {

	private String tranDateDdMonYyyy;
	private String partTranType;
	private String tranSubType;
	private String tranParticular;
	private String instrumentNo;
	private String instrumentDateDdMmYyyy;
	private BigDecimal amount;
	private String remarks;
	private String tranId;
	private String partTranSrlNum;
	private String tranParticular2;
	private String valueDateDdMmYyyy;
	private CounterpartyInfo counterpartyInfo;

	public TransactionDetail(String tranDateDdMonYyyy, String partTranType, String tranSubType, String tranParticular,
			String instrumentNo, String instrumentDateDdMmYyyy, BigDecimal amount, String remarks, String tranId,
			String partTranSrlNum, String tranParticular2, String valueDateDdMmYyyy) {
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

	public String getTranDateDdMonYyyy() {
		return tranDateDdMonYyyy;
	}

	public String getPartTranType() {
		return partTranType;
	}

	public String getTranSubType() {
		return tranSubType;
	}

	public String getTranParticular() {
		return tranParticular;
	}

	public String getInstrumentNo() {
		return instrumentNo;
	}

	public String getInstrumentDateDdMmYyyy() {
		return instrumentDateDdMmYyyy;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getRemarks() {
		return remarks;
	}

	public String getTranId() {
		return tranId;
	}

	public String getPartTranSrlNum() {
		return partTranSrlNum;
	}

	public String getTranParticular2() {
		return tranParticular2;
	}

	public String getValueDateDdMmYyyy() {
		return valueDateDdMmYyyy;
	}

	public CounterpartyInfo getCounterpartyInfo() {
		return counterpartyInfo;
	}

	public void setCounterpartyInfo(CounterpartyInfo counterpartyInfo) {
		this.counterpartyInfo = counterpartyInfo;
	}

	public void setTranDateDdMonYyyy(String tranDateDdMonYyyy) {
		this.tranDateDdMonYyyy = tranDateDdMonYyyy;
	}

	public void setPartTranType(String partTranType) {
		this.partTranType = partTranType;
	}

	public void setTranSubType(String tranSubType) {
		this.tranSubType = tranSubType;
	}

	public void setTranParticular(String tranParticular) {
		this.tranParticular = tranParticular;
	}

	public void setInstrumentNo(String instrumentNo) {
		this.instrumentNo = instrumentNo;
	}

	public void setInstrumentDateDdMmYyyy(String instrumentDateDdMmYyyy) {
		this.instrumentDateDdMmYyyy = instrumentDateDdMmYyyy;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public void setTranId(String tranId) {
		this.tranId = tranId;
	}

	public void setPartTranSrlNum(String partTranSrlNum) {
		this.partTranSrlNum = partTranSrlNum;
	}

	public void setTranParticular2(String tranParticular2) {
		this.tranParticular2 = tranParticular2;
	}

	public void setValueDateDdMmYyyy(String valueDateDdMmYyyy) {
		this.valueDateDdMmYyyy = valueDateDdMmYyyy;
	}
	
}
