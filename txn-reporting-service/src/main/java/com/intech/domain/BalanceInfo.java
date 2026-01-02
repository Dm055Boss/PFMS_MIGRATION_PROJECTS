package com.intech.domain;

import java.math.BigDecimal;

/**
 * Output of BAL query.
 */
public class BalanceInfo {

    private final String foracid;
    private final String bsrCode;
    private final String closeDateDdMmYyyySlashes;
    private final BigDecimal openingBalance;
    private final BigDecimal closingBalance;

    public BalanceInfo(String foracid,
                       String bsrCode,
                       String closeDateDdMmYyyySlashes,
                       BigDecimal openingBalance,
                       BigDecimal closingBalance) {
        this.foracid = foracid;
        this.bsrCode = bsrCode;
        this.closeDateDdMmYyyySlashes = closeDateDdMmYyyySlashes;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
    }

    public String getForacid() { return foracid; }

    public String getBsrCode() { return bsrCode; }

    public String getCloseDateDdMmYyyySlashes() { return closeDateDdMmYyyySlashes; }

    public BigDecimal getOpeningBalance() { return openingBalance; }

    public BigDecimal getClosingBalance() { return closingBalance; }
}
