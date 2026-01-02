package com.intech.domain;

public class TxnAccountCandidate {
	private final Long txnId;
	private final String accountNo;

	public TxnAccountCandidate(Long txnId, String accountNo) {
		this.txnId = txnId;
		this.accountNo = accountNo;
	}

	public Long getTxnId() {
		return txnId;
	}

	public String getAccountNo() {
		return accountNo;
	}
}
