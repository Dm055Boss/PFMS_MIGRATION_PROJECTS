// src/main/java/com/intech/EpayIniFileGenerator/service/IniXmlWriter.java
package com.intech.service;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.intech.config.IniProperties;
import com.intech.model.BatchPick;
import com.intech.model.CreditRow;
import com.intech.model.DebitRow;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IniXmlWriter {

  private final IniProperties ini;
  
  

  public IniXmlWriter(IniProperties ini) {
	super();
	this.ini = ini;
}

  // exact signature used by services
  public void write(String outDir, String msgId, BatchPick b, DebitRow d, List<CreditRow> credits) throws Exception {
    write(outDir, msgId, b, d, (Collection<? extends CreditRow>) credits);
  }

  public void write(String outDir, String msgId, BatchPick b, DebitRow d, Collection<? extends CreditRow> credits) throws Exception {
    var db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = db.newDocument();

    Element root = doc.createElement("InitiatedPayments"); doc.appendChild(root);
    attr(root,"xmlns","http://cpsms.com/InitiatedPaymentData");
    attr(root,"MessageId",msgId);
    attr(root,"PaymentProduct",nz(b.getProduct()));           // spec: mandatory
    attr(root,"Source",nz(b.getBankCode()));                  // spec: 3-digit bank code
    attr(root,"Destination","CPSMS");                         // spec: constant
    attr(root,"BankCode",nz(b.getBankCode()));                // spec: same as source
    attr(root,"BankName",nz(ini.getBankName()));              // spec: mandatory bank name
    attr(root,"RecordsCount", String.valueOf(credits.size())); // spec: total # of credit txns

    Element batch = el(doc,root,"BatchDetails");
    batch.setAttribute("CPSMSBatchNo", nz(b.getBatchNumber()));

    // DebitTransactions
    Element dtx = el(doc,batch,"DebitTransactions");
    dtx.setAttribute("C5756"," "+nz(d.getIfsc()));     // IFSC
    dtx.setAttribute("C6021"," "+nz(d.getAccNo()));    // Account No
    dtx.setAttribute("C5185","1");                     // # of debit transactions (always 1 here)
    dtx.setAttribute("C4063"," "+nz(d.getDebitAmt())); // total debit amount

    Element debit = el(doc,dtx,"Debit");
    txt(doc,debit,"C2006"," "+nz(d.getCpsmsDebitTran())); // cpsms debit tran id
    txt(doc,debit,"C2020"," "+nz(d.getInitUtr()));        // bank txn id / UTR
    txt(doc,debit,"C3501"," "+nz(d.getTs()));             // yyyymmddHHMMss
    txt(doc,debit,"C4063"," "+nz(d.getDebitAmt()));       // amount again (as per legacy)
    txt(doc,debit,"C6346"," "+nz(d.getC6346()));          // SUCC/HOLD/D0xx
    if (!"NA".equalsIgnoreCase(nz(d.getAuthMode()))) {
      txt(doc,debit,"AuthMode"," "+d.getAuthMode());      // BRNC/CINB (optional)
    }

    // CreditTransactions
    Element ctx = el(doc,batch,"CreditTransactions");
    ctx.setAttribute("C5185", String.valueOf(credits.size())); // total credit records

    for (CreditRow c : credits) {
      Element cl = el(doc,ctx,"Credit");
      txt(doc,cl,"C2006"," "+nz(c.getC2006()));
      txt(doc,cl,"C5569"," "+nz(c.getC5569()));
      txt(doc,cl,"C6061"," "+nz(c.getC6061()));
      if (nb(c.getUid()))     txt(doc,cl,"UID"," "+c.getUid());           // APBS only
      if (nb(c.getBankIin())) txt(doc,cl,"BankIIN"," "+c.getBankIin());   // APBS only
      txt(doc,cl,"C2020"," "+nz(c.getC2020()));
      txt(doc,cl,"C3501"," "+nz(c.getC3501()));
      txt(doc,cl,"C4038"," "+nz(c.getC4038()));
      txt(doc,cl,"C6346"," "+nz(c.getC6346()));                           // SUCC/INIT/DBFL/R11/…
      if (nb(c.getC6366()))   txt(doc,cl,"C6366"," "+c.getC6366());       // for R11 only
      if (nb(c.getC3380()))   txt(doc,cl,"C3380"," "+c.getC3380());       // settlement
      if (nb(c.getC3375()))   txt(doc,cl,"C3375"," "+c.getC3375());       // sender orig
      if (nb(c.getC3381()))   txt(doc,cl,"C3381"," "+c.getC3381());       // origination
      txt(doc,cl,"C6081"," "+nz(c.getC6081()));
      txt(doc,cl,"PmtRoute"," "+nz(c.getPmtroute()));
    }

    Path file = Paths.get(outDir).resolve(msgId + ".xml");
    var tf = TransformerFactory.newInstance().newTransformer();
    tf.setOutputProperty(OutputKeys.INDENT,"yes");
    tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
    try (OutputStream os = Files.newOutputStream(file)) {
      tf.transform(new DOMSource(doc), new StreamResult(os));
    }
  }

  private static Element el(Document d, Element p, String n){ Element e=d.createElement(n); p.appendChild(e); return e; }
  private static void attr(Element e,String k,String v){ e.setAttribute(k, v==null? "" : v); }
  private static void txt(Document d, Element p, String n, String v){ Element e=d.createElement(n); e.setTextContent(v==null? "" : v); p.appendChild(e); }
  private static String nz(String s){ return s==null? "" : s; }
  private static boolean nb(String s){ return s!=null && !s.isBlank(); }
}
