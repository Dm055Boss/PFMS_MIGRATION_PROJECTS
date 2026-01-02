package com.intech.EpayRejFileGenerator.model;

import java.math.BigDecimal;

/**
 * Represents one rejected credit row → one <RejectedPayment> node in XML.
 */
public class RejectedCreditRecord {

	private Long seqCreditId;

	private String c2020;
	private String c5756;
	private String c2006;
	private String c5569;
	private String c6061;
	private String uid;
	private String bankIIN;
	private String c6081;
	private String c5565;
	private BigDecimal amount; // numeric for total
	private String c3380;
	private String c3375;
	private String c3381;
	private String c6346;
	private String c6366;
	private String c7495;
	private String pmtRoute;

	public Long getSeqCreditId() {
		return seqCreditId;
	}

	public void setSeqCreditId(Long seqCreditId) {
		this.seqCreditId = seqCreditId;
	}

	public String getC2020() {
		return c2020;
	}

	public void setC2020(String c2020) {
		this.c2020 = c2020;
	}

	public String getC5756() {
		return c5756;
	}

	public void setC5756(String c5756) {
		this.c5756 = c5756;
	}

	public String getC2006() {
		return c2006;
	}

	public void setC2006(String c2006) {
		this.c2006 = c2006;
	}

	public String getC5569() {
		return c5569;
	}

	public void setC5569(String c5569) {
		this.c5569 = c5569;
	}

	public String getC6061() {
		return c6061;
	}

	public void setC6061(String c6061) {
		this.c6061 = c6061;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getBankIIN() {
		return bankIIN;
	}

	public void setBankIIN(String bankIIN) {
		this.bankIIN = bankIIN;
	}

	public String getC6081() {
		return c6081;
	}

	public void setC6081(String c6081) {
		this.c6081 = c6081;
	}

	public String getC5565() {
		return c5565;
	}

	public void setC5565(String c5565) {
		this.c5565 = c5565;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getC3380() {
		return c3380;
	}

	public void setC3380(String c3380) {
		this.c3380 = c3380;
	}

	public String getC3375() {
		return c3375;
	}

	public void setC3375(String c3375) {
		this.c3375 = c3375;
	}

	public String getC3381() {
		return c3381;
	}

	public void setC3381(String c3381) {
		this.c3381 = c3381;
	}

	public String getC6346() {
		return c6346;
	}

	public void setC6346(String c6346) {
		this.c6346 = c6346;
	}

	public String getC6366() {
		return c6366;
	}

	public void setC6366(String c6366) {
		this.c6366 = c6366;
	}

	public String getC7495() {
		return c7495;
	}

	public void setC7495(String c7495) {
		this.c7495 = c7495;
	}

	public String getPmtRoute() {
		return pmtRoute;
	}

	public void setPmtRoute(String pmtRoute) {
		this.pmtRoute = pmtRoute;
	}
}
