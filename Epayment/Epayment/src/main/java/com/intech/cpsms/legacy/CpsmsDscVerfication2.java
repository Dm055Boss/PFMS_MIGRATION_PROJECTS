package com.intech.cpsms.legacy;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.ByteArrayInputStream;
/**
 *
 * @amarjeet kumar
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/*import Validate.KeyValueKeySelector;
import Validate.SimpleKeySelectorResult;*/
public class CpsmsDscVerfication2 {

	public static void main(String[] args) throws Exception {
		System.out.println(valdiatexml("D:\\PFMS\\ePay\\PaymentReqData\\FromCPSMS\\056DSCPAYREQ150920231.xml"));
	}

	// ADDED: shared algorithm matcher for both selectors
	private static boolean algMatches(String sigAlgUri, String keyAlg) {
		if (keyAlg == null || sigAlgUri == null)
			return false;
		// DSA with SHA1 (legacy)
		if (keyAlg.equalsIgnoreCase("DSA") && sigAlgUri.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
			return true;
		}
		// RSA — accept SHA1 (legacy) and SHA256 (modern)
		if (keyAlg.equalsIgnoreCase("RSA") && (sigAlgUri.equalsIgnoreCase(SignatureMethod.RSA_SHA1)
				|| sigAlgUri.equalsIgnoreCase("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"))) {
			return true;
		}
		return false;
	}

	public static String valdiatexml(String xmlPath) throws Exception {
		String resDsc = "";
		try {
			String fileName = xmlPath;

			// Instantiate the document to be validated
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			Document doc = dbf.newDocumentBuilder().parse(new FileInputStream(fileName));

			// Find Signature element
			NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
			if (nl.getLength() == 0) {
				throw new Exception("Cannot find Signature element");
			}

			// Create a DOM XMLSignatureFactory that will be used to unmarshal the document
			XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
			System.out.println("----- " + fac.toString());

			// ADDED: load fallback public key (from your configured cert) once, to use if
			// KeyInfo lacks usable keys
			X509Certificate fallbackCert = getCertFromFile(); // ADDED
			PublicKey fallbackPk = fallbackCert.getPublicKey(); // ADDED

			// CHANGED: use a KeySelector that tries KeyValue/X509Data first, then falls
			// back to your configured cert
			DOMValidateContext valContext = new DOMValidateContext(new CompositeKeySelector(fallbackPk), // CHANGED
					nl.item(0));

			// ADDED: allow legacy rsa-sha1 signatures to be unmarshalled/validated
			valContext.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.FALSE); // ADDED

			// unmarshal the XMLSignature
			XMLSignature signature = fac.unmarshalXMLSignature(valContext);

			// Validate the XMLSignature
			boolean coreValidity = signature.validate(valContext);
			System.out.println("coreValidity::" + coreValidity);

			if (!coreValidity) {
				boolean sv = signature.getSignatureValue().validate(valContext);
				resDsc = sv ? "valid" : "invalid";
				System.out.println("signature validation status: " + sv);

				// check the validation status of each Reference
				Iterator<?> i = signature.getSignedInfo().getReferences().iterator();
				for (int j = 0; i.hasNext(); j++) {
					boolean refValid = ((Reference) i.next()).validate(valContext);
					System.out.println("ref[" + j + "] validity status: " + refValid);
				}
			} else {
				System.out.println("Signature passed core validation");
				resDsc = "valid";
			}
		} catch (Exception e) {
			e.printStackTrace();
			resDsc = "invalid";
		}
		System.out.println("resDsc::" + resDsc);
		return resDsc;
	}

	/**
	 * KeySelector which retrieves the public key out of KeyInfo if present
	 * (KeyValue or X509Data), otherwise falls back to your configured public
	 * certificate. NOTE: We also accept RSA-SHA1 along with RSA-SHA256 (legacy
	 * payloads).
	 */
	// ADDED: new selector that tries KeyValue -> X509Data -> fallbackPk
	private static class CompositeKeySelector extends KeySelector { // ADDED
		private final PublicKey fallbackPk; // ADDED

		CompositeKeySelector(PublicKey fallbackPk) {
			this.fallbackPk = fallbackPk;
		} // ADDED

		public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method,
				XMLCryptoContext context) throws KeySelectorException {

			if (keyInfo != null) {
				List<?> list = keyInfo.getContent();
				SignatureMethod sm = (SignatureMethod) method;

				// 1) Try KeyValue
				for (Object obj : list) {
					if (obj instanceof KeyValue kv) {
						try {
							PublicKey pk = kv.getPublicKey();
							if (algMatches(sm.getAlgorithm(), pk.getAlgorithm())) {
							    return new SimpleKeySelectorResult(pk);
							}
						} catch (KeyException ignored) {
							/* try next */ }
					}
				}

				// 2) Try X509Data (extract cert -> public key)
				for (Object obj : list) {
					if (obj instanceof X509Data xd) {
						for (Object xo : xd.getContent()) {
							if (xo instanceof X509Certificate cert) {
								PublicKey pk = cert.getPublicKey();
								if (algMatches(sm.getAlgorithm(), pk.getAlgorithm())) {
								    return new SimpleKeySelectorResult(pk);
								}
							}
						}
					}
				}
			}

			// 3) Fallback to configured cert's public key
			if (fallbackPk != null) {
				return new SimpleKeySelectorResult(fallbackPk);
			}

			throw new KeySelectorException("No KeyValue/X509Data and no fallback public key available");
		}
	}

	/**
	 * (kept for compatibility) Original KeySelector — now unused by default. Left
	 * here in case any other legacy code references it.
	 */
	@SuppressWarnings("unused")
	private static class KeyValueKeySelector extends KeySelector {
		public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method,
				XMLCryptoContext context) throws KeySelectorException {

			if (keyInfo == null) {
				throw new KeySelectorException("Null KeyInfo object!");
			}
			SignatureMethod sm = (SignatureMethod) method;
			List<?> list = keyInfo.getContent();

			for (int i = 0; i < list.size(); i++) {
				XMLStructure xmlStructure = (XMLStructure) list.get(i);
				if (xmlStructure instanceof X509Data) {
					PublicKey pk = null;
					X509Certificate cert = null;
					List<?> l = ((X509Data) xmlStructure).getContent();
					if (l.size() > 0) {
						try {
							cert = getCertFromFile();
						} catch (Exception e) {
							e.printStackTrace();
						}
						pk = cert.getPublicKey();
						// CHANGED: accept RSA-SHA1 as well as RSA-SHA256
						/*
						 * if (algEquals(sm.getAlgorithm(), pk.getAlgorithm())) { return new
						 * SimpleKeySelectorResult(pk); }
						 */
					}
				}
				if (xmlStructure instanceof KeyValue) {
					PublicKey pk = null;
					try {
						pk = ((KeyValue) xmlStructure).getPublicKey();
					} catch (KeyException ke) {
						throw new KeySelectorException(ke);
					}
					// CHANGED: accept RSA-SHA1 as well as RSA-SHA256
					if (algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
						return new SimpleKeySelectorResult(pk);
					}
				}
			}
			// CHANGED: instead of throwing, fallback to configured cert
			try { // CHANGED
				X509Certificate cert = getCertFromFile(); // CHANGED
				return new SimpleKeySelectorResult(cert.getPublicKey()); // CHANGED
			} catch (Exception ex) { // CHANGED
				throw new KeySelectorException("No KeyValue element found!", ex); // CHANGED
			}
		}

		// CHANGED: accept RSA-SHA1 and RSA-SHA256 (legacy + modern)
		static boolean algEquals(String algURI, String algName) {
			if (algName.equalsIgnoreCase("DSA") && algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
				return true;
			} else if (algName.equalsIgnoreCase("RSA") && (algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1) // ADDED
					|| algURI.equalsIgnoreCase("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"))) {
				return true;
			} else {
				return false;
			}
		}
	}

	private static class SimpleKeySelectorResult implements KeySelectorResult {
		private PublicKey pk;

		SimpleKeySelectorResult(PublicKey pk) {
			this.pk = pk;
		}

		public Key getKey() {
			return pk;
		}
	}

	private static X509Certificate getCertFromFile() throws Exception {
		ResourceBundle bundle = ResourceBundle.getBundle("FROM_CPSMS_FOLDER_MAP");
		String PUBLIC = (bundle.getString("PUBLICKEYPATH")).trim();
		String PubKey = (bundle.getString("PUBLICKEYNAME")).trim();
		System.out.println("-------");
		String path = PUBLIC + "/" + PubKey;
		System.out.println("----" + path);
		InputStream is = new FileInputStream(new File(path));
		byte[] buffer = new byte[is.available()];
		is.read(buffer);
		is.close();
		// System.out.println("Encoded Cert : " + buffer.length + " bytes");
		ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
		java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
		Certificate cert = cf.generateCertificate(bis);
		// System.out.println("Certificate : " + cert);
		return (java.security.cert.X509Certificate) cert;
	}
}
