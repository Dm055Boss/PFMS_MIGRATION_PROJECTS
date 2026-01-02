package com.intech.cpsms.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML helpers: secure parse, namespace-aware XPath, XSD validation, signature
 * detection, safe getters, pretty print, and file utilities.
 */
public final class XmlUtil {

	private XmlUtil() {
	}

	// Common namespace URIs
	public static final String NS_DS = "http://www.w3.org/2000/09/xmldsig#";

	// -----------------------
	// Secure DOM parse
	// -----------------------
	public static Document parse(Path xmlFile) throws Exception {
		try (InputStream is = Files.newInputStream(xmlFile)) {
			return parse(is);
		}
	}

	public static Document parse(InputStream is) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		// Secure settings to prevent XXE / SSRF
		dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		safeSet(dbf, "http://apache.org/xml/features/disallow-doctype-decl", true);
		safeSet(dbf, "http://xml.org/sax/features/external-general-entities", false);
		safeSet(dbf, "http://xml.org/sax/features/external-parameter-entities", false);
		safeSet(dbf, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		dbf.setXIncludeAware(false);
		dbf.setExpandEntityReferences(false);
		dbf.setNamespaceAware(true);

		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(is);
	}

	private static void safeSet(DocumentBuilderFactory dbf, String feature, boolean value) {
		try {
			dbf.setFeature(feature, value);
		} catch (Throwable ignored) {
		}
	}

	// -----------------------
	// XPath + namespaces
	// -----------------------
	public static XPath newXPath(Map<String, String> ns) {
		XPath xp = XPathFactory.newInstance().newXPath();
		if (ns != null && !ns.isEmpty()) {
			xp.setNamespaceContext(new MapNamespaceContext(ns));
		}
		return xp;
	}

	public static Node xpathNode(Node root, String expr, Map<String, String> ns) throws XPathExpressionException {
		return (Node) newXPath(ns).evaluate(expr, root, XPathConstants.NODE);
	}

	public static NodeList xpathNodes(Node root, String expr, Map<String, String> ns) throws XPathExpressionException {
		return (NodeList) newXPath(ns).evaluate(expr, root, XPathConstants.NODESET);
	}

	public static String xpathString(Node root, String expr, Map<String, String> ns) throws XPathExpressionException {
		String v = (String) newXPath(ns).evaluate(expr, root, XPathConstants.STRING);
		return v != null ? v.trim() : null;
	}

	// -----------------------
	// Element & attribute helpers
	// -----------------------
	public static String getAttr(Element e, String name) {
		return e != null && e.hasAttribute(name) ? e.getAttribute(name).trim() : null;
	}

	public static String text(Node n) {
		return n == null ? null : n.getTextContent() == null ? null : n.getTextContent().trim();
	}

	public static List<Element> childrenByTagNS(Element parent, String ns, String localName) {
		List<Element> out = new ArrayList<>();
		if (parent == null)
			return out;
		NodeList nl = parent.getElementsByTagNameNS(ns, localName);
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n instanceof Element el && el.getParentNode() == parent) {
				out.add(el);
			}
		}
		return out;
	}

	public static List<Element> asElementList(NodeList nl) {
		List<Element> out = new ArrayList<>();
		if (nl == null)
			return out;
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n instanceof Element)
				out.add((Element) n);
		}
		return out;
	}

	// -----------------------
	// Type coercion (safe)
	// -----------------------
	public static Integer toInt(String s) {
		if (s == null || s.isBlank())
			return null;
		try {
			return Integer.valueOf(s.trim());
		} catch (Exception e) {
			return null;
		}
	}

	public static Long toLong(String s) {
		if (s == null || s.isBlank())
			return null;
		try {
			return Long.valueOf(s.trim());
		} catch (Exception e) {
			return null;
		}
	}

	public static BigDecimal toBigDecimal(String s) {
		if (s == null || s.isBlank())
			return null;
		try {
			return new BigDecimal(s.trim());
		} catch (Exception e) {
			return null;
		}
	}

	public static LocalDate toDate(String s, String pattern) {
		if (s == null || s.isBlank())
			return null;
		try {
			return LocalDate.parse(s.trim(), DateTimeFormatter.ofPattern(pattern));
		} catch (Exception e) {
			return null;
		}
	}

	// -----------------------
	// Signature detection
	// -----------------------
	public static boolean hasSignature(Document doc) {
		if (doc == null)
			return false;
		NodeList nl = doc.getElementsByTagNameNS(NS_DS, "Signature");
		return nl != null && nl.getLength() > 0;
	}

	// -----------------------
	// XSD validation
	// -----------------------
	public static void validateWithXsd(Document doc, Path xsdPath) throws Exception {
		try (InputStream is = Files.newInputStream(xsdPath)) {
			validateWithXsd(doc, is);
		}
	}

	public static void validateWithXsd(Document doc, InputStream xsdStream) throws Exception {
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		// secure schema factory
		sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		Schema schema = sf.newSchema(new StreamSource(xsdStream));
		Validator v = schema.newValidator();
		v.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		v.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		v.validate(new DOMSource(doc));
	}

	// -----------------------
	// Pretty print / write
	// -----------------------
	public static String toString(Document doc) throws TransformerException {
		Transformer tf = newIndentingTransformer();
		StringWriter sw = new StringWriter();
		tf.transform(new DOMSource(doc), new StreamResult(sw));
		return sw.toString();
	}

	public static void writeString(Path target, String xml) throws IOException {
		Files.createDirectories(target.getParent());
		Files.writeString(target, xml, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING);
	}

	public static void writeDocument(Path target, Document doc) throws Exception {
		Transformer tf = newIndentingTransformer();
		Files.createDirectories(target.getParent());
		try (OutputStream os = Files.newOutputStream(target, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			tf.transform(new DOMSource(doc), new StreamResult(os));
		}
	}

	private static Transformer newIndentingTransformer() throws TransformerConfigurationException {
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		} catch (Throwable ignored) {
		}
		Transformer t = tf.newTransformer();
		t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		return t;
	}

	// -----------------------
	// Checksums / utils
	// -----------------------
	public static String sha256(Path file) throws Exception {
		try (InputStream is = Files.newInputStream(file)) {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] buf = new byte[8192];
			int r;
			while ((r = is.read(buf)) != -1)
				md.update(buf, 0, r);
			byte[] dig = md.digest();
			StringBuilder sb = new StringBuilder(dig.length * 2);
			for (byte b : dig)
				sb.append(String.format("%02x", b));
			return sb.toString();
		}
	}

	// -----------------------
	// Namespace context
	// -----------------------
	public static final class MapNamespaceContext implements NamespaceContext {
		private final Map<String, String> map;

		public MapNamespaceContext(Map<String, String> map) {
			this.map = new HashMap<>(map);
		}

		@Override
		public String getNamespaceURI(String prefix) {
			if (prefix == null)
				return XMLConstants.NULL_NS_URI;
			return map.getOrDefault(prefix, XMLConstants.NULL_NS_URI);
		}

		@Override
		public String getPrefix(String namespaceURI) {
			for (Map.Entry<String, String> e : map.entrySet()) {
				if (Objects.equals(e.getValue(), namespaceURI))
					return e.getKey();
			}
			return null;
		}

		@Override
		public Iterator<String> getPrefixes(String namespaceURI) {
			List<String> out = new ArrayList<>();
			for (Map.Entry<String, String> e : map.entrySet()) {
				if (Objects.equals(e.getValue(), namespaceURI))
					out.add(e.getKey());
			}
			return out.iterator();
		}
	}
}
