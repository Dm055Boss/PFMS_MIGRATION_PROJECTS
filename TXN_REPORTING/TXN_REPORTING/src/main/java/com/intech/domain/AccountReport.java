package com.intech.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * One <Account> section in the generated TXN XML.
 */
public class AccountReport {

    private final BalanceInfo balanceInfo;
    private final int transactionCount;
    private final List<TransactionDetail> transactions = new ArrayList<>();

    public AccountReport(BalanceInfo balanceInfo, int transactionCount) {
        this.balanceInfo = balanceInfo;
        this.transactionCount = transactionCount;
    }

    public BalanceInfo getBalanceInfo() { return balanceInfo; }

    public int getTransactionCount() { return transactionCount; }

    public List<TransactionDetail> getTransactions() { return transactions; }
}
