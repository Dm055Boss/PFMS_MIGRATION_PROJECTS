package com.intech.EpayIniFileGenerator.model;

public class CreditRow {
  private Long   seqCreditId;
  private String c2006, c5569, c6061, uid, bankIin, c6081, c2020, c3501, c4038, c6366, c6346, c3380, c3375, c3381, pmtroute;

  public CreditRow() {}

  // Full 16-arg ctor (kept for compatibility)
  public CreditRow(Long seqCreditId, String c2006, String c5569, String c6061, String uid, String bankIin,
                   String c6081, String c2020, String c3501, String c4038, String c6366, String c6346,
                   String c3380, String c3375, String c3381, String pmtroute) {
    this.seqCreditId = seqCreditId; this.c2006 = c2006; this.c5569 = c5569; this.c6061 = c6061; this.uid = uid;
    this.bankIin = bankIin; this.c6081 = c6081; this.c2020 = c2020; this.c3501 = c3501; this.c4038 = c4038;
    this.c6366 = c6366; this.c6346 = c6346; this.c3380 = c3380; this.c3375 = c3375; this.c3381 = c3381; this.pmtroute = pmtroute;
  }

  public CreditRow(Long seqCreditId,
          String c2006, String c5569, String c6061, String uid, String bankIin,
          String c6081, String c2020, String c3501, String c4038,
          String c6366, String c6346, String c3380, String c3375) {
// default the missing tail fields
this(seqCreditId,
c2006, c5569, c6061, uid, bankIin,
c6081, c2020, c3501, c4038,
c6366, c6346, c3380, c3375,
/* c3381 */ "", /* pmtroute */ "NA");
}
  
  /** Tolerant mapper: works with 14/15/16 selected columns. */
  public static CreditRow fromRow(Object[] r) {
    CreditRow c = new CreditRow();
    c.seqCreditId = asLong(r, 0);
    c.c2006       = asStr (r, 1);
    c.c5569       = asStr (r, 2);
    c.c6061       = asStr (r, 3);
    c.uid         = asStr (r, 4);
    c.bankIin     = asStr (r, 5);
    c.c6081       = asStr (r, 6);
    c.c2020       = asStr (r, 7);
    c.c3501       = asStr (r, 8);
    c.c4038       = asStr (r, 9);
    c.c6366       = asStr (r,10);
    c.c6346       = asStr (r,11);
    c.c3380       = asStr (r,12);
    c.c3375       = asStr (r,13);
    // slot 14: either C3381 or (if query returned only 15 cols) PmtRoute
    String maybe14 = asStr(r,14);
    String maybe15 = asStr(r,15);
    if (r.length >= 16) {
      c.c3381   = maybe14;
      c.pmtroute= maybe15;
    } else if (r.length == 15) {
      // assume C3381 omitted, PmtRoute present at index 14
      c.c3381   = "";
      c.pmtroute= maybe14;
    } else { // r.length == 14 (no C3381, no PmtRoute)
      c.c3381   = "";
      c.pmtroute= "NA";
    }
    return c;
  }

  private static String asStr(Object[] r, int i) { return (i < r.length && r[i] != null) ? r[i].toString() : ""; }
  private static Long   asLong(Object[] r, int i) {
    if (i >= r.length || r[i] == null) return null;
    return (r[i] instanceof Number n) ? n.longValue() : Long.valueOf(r[i].toString());
  }

  // getters (used by XmlWriter)
  public Long getSeqCreditId(){ return seqCreditId; }
  public String getC2006(){ return c2006; }  public String getC5569(){ return c5569; }
  public String getC6061(){ return c6061; }  public String getUid(){ return uid; }
  public String getBankIin(){ return bankIin; } public String getC6081(){ return c6081; }
  public String getC2020(){ return c2020; }  public String getC3501(){ return c3501; }
  public String getC4038(){ return c4038; }  public String getC6366(){ return c6366; }
  public String getC6346(){ return c6346; }  public String getC3380(){ return c3380; }
  public String getC3375(){ return c3375; }  public String getC3381(){ return c3381; }
  public String getPmtroute(){ return pmtroute; }
}
