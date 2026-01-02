package com.intech.domain;

/**
 * Counterparty details (legacy XCNT + KBDT output).
 */
public class CounterpartyInfo {

	private String accountNumber;
	private String accountName;
	private String bankName;
	private String bankBranchCode;

	public CounterpartyInfo(String accountNumber, String accountName, String bankName, String bankBranchCode) {
		this.accountNumber = accountNumber;
		this.accountName = accountName;
		this.bankName = bankName;
		this.bankBranchCode = bankBranchCode;
	}
	
	

	public CounterpartyInfo() {
		super();
		// TODO Auto-generated constructor stub
	}



	public String getAccountNumber() {
		return accountNumber;
	}

	public String getAccountName() {
		return accountName;
	}

	public String getBankName() {
		return bankName;
	}

	public String getBankBranchCode() {
		return bankBranchCode;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public void setBankBranchCode(String bankBranchCode) {
		this.bankBranchCode = bankBranchCode;
	}
	
	
}
