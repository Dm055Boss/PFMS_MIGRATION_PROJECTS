package com.intech.EXAckReader.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.intech.EXAckReader.model.AckExceptionRecord;

/**
 * Parses the Exceptions XML (namespace aware).
 * Production safe: disables XXE.
 */
@Component
public class AckXmlParser {
    private static final Logger log = LoggerFactory.getLogger(AckXmlParser.class);

    // Based on your sample file:
    private static final String NS = "http://cpsms.com/TransactionsDataException";

    public List<AckExceptionRecord> parse(Path xmlFile) throws Exception {
        Document doc;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        // Security hardening (XXE protection)
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);

        DocumentBuilder builder = dbf.newDocumentBuilder();

        try (InputStream in = Files.newInputStream(xmlFile)) {
            doc = builder.parse(in);
        }

        Element root = doc.getDocumentElement(); // <Exceptions ...>

        String messageId = safeAttr(root, "MessageId");
        String originalMessageId = safeAttr(root, "OriginalMessageId");

        NodeList exceptionNodes = root.getElementsByTagNameNS(NS, "Exception");
        List<AckExceptionRecord> records = new ArrayList<>();

        for (int i = 0; i < exceptionNodes.getLength(); i++) {
            Element ex = (Element) exceptionNodes.item(i);

            String accountNumber = text(ex, "AccountNumber");
            String remarks = text(ex, "Remarks");
            String reconType = text(ex, "ReconciliationType");

            // Build one record
            AckExceptionRecord r = new AckExceptionRecord(
                    originalMessageId,
                    messageId,
                    accountNumber,
                    remarks,
                    reconType
            );
            records.add(r);
        }

        if (records.isEmpty()) {
            log.warn("No <Exception> nodes found in file: {}", xmlFile.getFileName());
        }
        return records;
    }

    private static String safeAttr(Element el, String attr) {
        if (el == null) return null;
        String v = el.getAttribute(attr);
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static String text(Element parent, String localName) {
        NodeList nl = parent.getElementsByTagNameNS(NS, localName);
        if (nl == null || nl.getLength() == 0) return null;
        String v = nl.item(0).getTextContent();
        return (v == null) ? null : v.trim();
    }
}
