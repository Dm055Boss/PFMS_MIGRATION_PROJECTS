package com.intech.EpayIniFileGenerator.model;

/**
 * Matches Debit query mapping. Also includes initUtr & c6346 used by XmlWriter;
 * the 6-arg ctor defaults them to "" so your existing code compiles.
 */
public class DebitRow {
  private String ifsc;             // C5756
  private String accNo;            // C6021
  private String ts;               // C3501 (yyyymmddHH24miss)
  private String debitAmt;         // C4063
  private String cpsmsDebitTran;   // C2006
  private String authMode;         // AuthMode (BRNC/CINB/NA)
  private String initUtr = "";     // C2020 (optional in some flows)
  private String c6346  = "";      // C6346 (SUCC/DBFL/INIT...)

  public DebitRow() {}

  /** Constructor your BatchQuery currently uses (6 Strings). */
  public DebitRow(String ifsc, String accNo, String ts, String debitAmt,
                  String cpsmsDebitTran, String authMode) {
    this.ifsc = ifsc;
    this.accNo = accNo;
    this.ts = ts;
    this.debitAmt = debitAmt;
    this.cpsmsDebitTran = cpsmsDebitTran;
    this.authMode = authMode;
  }

  /** Full constructor (if you later fetch initUtr/c6346 from SQL). */
  public DebitRow(String ifsc, String accNo, String ts, String debitAmt,
                  String cpsmsDebitTran, String authMode, String initUtr, String c6346) {
    this(ifsc, accNo, ts, debitAmt, cpsmsDebitTran, authMode);
    this.initUtr = initUtr;
    this.c6346 = c6346;
  }

  public String getIfsc() { return ifsc; }
  public String getAccNo() { return accNo; }
  public String getTs() { return ts; }
  public String getDebitAmt() { return debitAmt; }
  public String getCpsmsDebitTran() { return cpsmsDebitTran; }
  public String getAuthMode() { return authMode; }
  public String getInitUtr() { return initUtr; }
  public String getC6346() { return c6346; }

  public void setIfsc(String ifsc) { this.ifsc = ifsc; }
  public void setAccNo(String accNo) { this.accNo = accNo; }
  public void setTs(String ts) { this.ts = ts; }
  public void setDebitAmt(String debitAmt) { this.debitAmt = debitAmt; }
  public void setCpsmsDebitTran(String cpsmsDebitTran) { this.cpsmsDebitTran = cpsmsDebitTran; }
  public void setAuthMode(String authMode) { this.authMode = authMode; }
  public void setInitUtr(String initUtr) { this.initUtr = initUtr; }
  public void setC6346(String c6346) { this.c6346 = c6346; }
}
