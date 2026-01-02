package com.intech.TxnAckReader.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

@Component
public class AckXmlParser {

    public static class AckData {
        public final String ackMessageId;     // Acknowledgement/@MessageId  -> TXNREQACK
        public final String txnReqFn;         // OriginalMessageId (without .xml) -> WHERE TXNREQFN
        public final String responseCode;     // -> TXN_RESCODE
        public final String errorCodeOrNull;  // -> TXN_ERRCODE (nullable)

        public AckData(String ackMessageId, String txnReqFn, String responseCode, String errorCodeOrNull) {
            this.ackMessageId = ackMessageId;
            this.txnReqFn = txnReqFn;
            this.responseCode = responseCode;
            this.errorCodeOrNull = errorCodeOrNull;
        }
    }

    public AckData parse(Path xmlFile) throws Exception {
        try (InputStream in = Files.newInputStream(xmlFile)) {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            // Secure parse (no XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            Document doc = dbf.newDocumentBuilder().parse(in);

            var xp = XPathFactory.newInstance().newXPath();

            String ackMessageId = (String) xp.evaluate(
                    "/*[local-name()='Acknowledgement']/@MessageId",
                    doc, XPathConstants.STRING);

            String original = (String) xp.evaluate(
                    "//*[local-name()='OriginalMessageId']/text()",
                    doc, XPathConstants.STRING);

            String responseCode = (String) xp.evaluate(
                    "//*[local-name()='ResponseCode']/text()",
                    doc, XPathConstants.STRING);

            String errorCode = (String) xp.evaluate(
                    "//*[local-name()='ErrorCode']/text()",
                    doc, XPathConstants.STRING);

            String txnReqFn = normalizeOriginal(original);
            String err = (errorCode == null || errorCode.trim().isEmpty()) ? null : errorCode.trim();

            return new AckData(
                    ackMessageId == null ? null : ackMessageId.trim(),
                    txnReqFn,
                    responseCode == null ? null : responseCode.trim(),
                    err
            );
        }
    }

    private String normalizeOriginal(String original) {
        if (original == null) return null;
        String s = original.trim();
        if (s.toLowerCase().endsWith(".xml")) {
            return s.substring(0, s.length() - 4);
        }
        return s;
    }
}
