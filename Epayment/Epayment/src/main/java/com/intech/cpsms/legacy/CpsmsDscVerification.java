package com.intech.cpsms.legacy;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @amarjeet kumar
 */
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.List;
import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
public class CpsmsDscVerification {
	
	static Logger log = LogManager.getLogger(CpsmsDscVerification.class);

	public static void main(String[] args) throws Exception {       
		//System.out.println(valdiatexml("D:\\PFMS\\EenrolmentDsc\\056DISCREQ190820192.xml"));
		 System.out.println(valdiatexml("D:\\D drive\\shared folder\\Document\\IBKCPSMS\\dcb\\signture val sha2\\SIG FILE\\056DSCPAYREQ150920231.xml"));
	}

	public static String valdiatexml(String xmlPath) throws Exception {   
		System.out.println("Came for DSC Verifiaction-->");
		String resDsc="";
		try {
		log.info("AT Digi--"+xmlPath);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder builder = dbf.newDocumentBuilder();
		Document doc = builder.parse(new FileInputStream(xmlPath));
		File xml_file=new File(xmlPath);
		String file_type=xml_file.getName().toString().substring(3, 6);
		log.info("SIGNATURE VALIDAION CLASS->"+xml_file.getName()+"--"+file_type);
		System.out.println("SIGNATURE VALIDAION CLASS->"+xml_file.getName()+"--"+file_type);
		NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
		//System.out.println(nl.getLength());
		//System.out.println(nl.item(1));
		
		if (nl.getLength() == 0) 
		{
			
			
			throw new Exception("Cannot find Signature element");
		}
		
		DOMValidateContext valContext=null;
		/* if(file_type.equalsIgnoreCase("DSC"))
        { valContext = new DOMValidateContext(new DscKeyValueKeySelector(), nl.item(0));}
        else{ valContext = new DOMValidateContext(new KeyValueKeySelector(), nl.item(0));}
        XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
        XMLSignature signature = factory.unmarshalXMLSignature(valContext);
        if(signature.validate(valContext)==true)
         resDsc="valid";
        else resDsc="invalid";  
		 */
		for(int i=1;i<=nl.getLength();i++){
		
			
			if(file_type.equalsIgnoreCase("DSC")){
				log.info("check sign tag count-->"+nl.item(i-0));    
				System.out.println("check sign tag count-->"+nl.item(i-0));
				valContext = new DOMValidateContext(new DscKeyValueKeySelector(),nl.item(0));
				
			}else{ 
				valContext = new DOMValidateContext(new KeyValueKeySelector(),nl.item(0));
				System.out.println("INSIDE ELSE");
			}
			
			XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
			XMLSignature signature = factory.unmarshalXMLSignature(valContext);
			
			if(signature.validate(valContext)==true){
				resDsc="valid";
			}else{
				resDsc="invalid";   
			} 
			
			log.info("-Res-"+resDsc);
		} 
		} catch(Exception e) {
			System.out.println("handle here for sha2");
    		try {
    			resDsc = CpsmsDscVerfication2.valdiatexml(xmlPath);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return resDsc;
	}

	private static class KeyValueKeySelector extends KeySelector {

		public KeySelectorResult select(KeyInfo keyInfo,
				KeySelector.Purpose purpose,
				AlgorithmMethod method,
				XMLCryptoContext context)
						throws KeySelectorException {

			if (keyInfo == null) {
				throw new KeySelectorException("Null KeyInfo object!");
			}
			SignatureMethod sm = (SignatureMethod) method;
			//System.out.println(sm);
			List list = keyInfo.getContent();
			for (int i = 0; i < list.size(); i++) {
				XMLStructure xmlStructure = (XMLStructure) list.get(i);
				if (xmlStructure instanceof KeyValue) 
				{
					PublicKey pk = null;
					try {
						pk = ((KeyValue)xmlStructure).getPublicKey();
					} catch (KeyException ke) {
						throw new KeySelectorException(ke);
					}
					// make sure algorithm is compatible with method
					if (algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
						return new SimpleKeySelectorResult(pk);
					}
				} else if (xmlStructure instanceof X509Data) {
					for (Object data : ((X509Data) xmlStructure).getContent()) {
						if (data instanceof X509Certificate) 
						{
							//PublicKey pk = ((X509Certificate) data).getPublicKey();
							//if public key get from xml file then comment below public key selection part ann uncomment above public key selection part
							PublicKey pk = null;
							try {
								pk = getCertFromFile().getPublicKey();
							} catch (Exception e) {
								log.error("Error getting public key from file.", e);
								System.exit(10);
							}
							if (algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
								return new SimpleKeySelectorResult(pk);
							}
						}
					}
				}
			}
			throw new KeySelectorException("No KeyValue element found!");
		}

		static boolean algEquals(String algURI, String algName) {
			if (algName.equalsIgnoreCase("DSA")
					&& algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
				return true;
			} else if (algName.equalsIgnoreCase("RSA")
					&& algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
				return true;
			} else {
				return false;
			}
		}
	}

	private static class DscKeyValueKeySelector extends KeySelector
	{
		public KeySelectorResult select(KeyInfo keyInfo,KeySelector.Purpose purpose,AlgorithmMethod method,XMLCryptoContext context)throws KeySelectorException 
		{
			log.info("--DSC------");
			if (keyInfo == null) 
			{
				throw new KeySelectorException("Null KeyInfo object!");
			}
			SignatureMethod sm = (SignatureMethod) method;
			List list = keyInfo.getContent();
			log.info("Pk--1");
			for (int i = 0; i < list.size(); i++) 
			{
				log.info("Pk--2");
				XMLStructure xmlStructure = (XMLStructure) list.get(i);
				if (xmlStructure instanceof KeyValue) 
				{
					PublicKey pk = null;
					try 
					{
						pk = ((KeyValue)xmlStructure).getPublicKey();
						log.info("Pk--"+pk);
					}
					catch (KeyException ke) 
					{
						throw new KeySelectorException(ke);
					}
					// make sure algorithm is compatible with method
					if (algEquals(sm.getAlgorithm(), pk.getAlgorithm())) 
					{
						return new SimpleKeySelectorResult(pk);
					}
				}
				else if (xmlStructure instanceof X509Data) 
				{    log.info("else--");
				for (Object data : ((X509Data) xmlStructure).getContent()) 
				{
					if (data instanceof X509Certificate) 
					{
						PublicKey pk = ((X509Certificate) data).getPublicKey();
						log.info("Pk-f-"+pk);
						//if public key get from xml file then comment below public key selection part ann uncomment above public key selection part
						/* PublicKey pk = null;
                            try {
                                pk = getCertFromFile().getPublicKey();
                            } catch (Exception e) {
                                System.err.println("Error getting public key from file.");
                                e.printStackTrace();
                                System.exit(10);
                            }*/
						if (algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
							return new SimpleKeySelectorResult(pk);
						}
					}
				}
				}
			}
			throw new KeySelectorException("No KeyValue element found!");
		}

		static boolean algEquals(String algURI, String algName) {
			if (algName.equalsIgnoreCase("DSA")
					&& algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
				return true;
			} else if (algName.equalsIgnoreCase("RSA")
					&& algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
				return true;
			} else {
				return false;
			}
		}
	} 

	private static X509Certificate getCertFromFile() throws Exception
	{   
		ResourceBundle xrbdir = ResourceBundle.getBundle("FROM_CPSMS_FOLDER_MAP");; 
 	    String Db_details=xrbdir.getString("DATABASE");
 	    Properties prop = new Properties();
 	    File p = new File(Db_details);
 	    prop.load(new FileInputStream(p));    
		String PUBLIC=(prop.getProperty("PUBLICKEYPATH")).trim();
		String PubKey=(prop.getProperty("PUBLICKEYNAME")).trim();
		//System.out.println("-------");
		String path = PUBLIC+"/"+PubKey;
		//System.out.println("----"+path);
		InputStream is = new FileInputStream(new File(path));
		byte[] buffer = new byte[is.available()];
		is.read(buffer);
		is.close();
		//System.out.println("Encoded Cert : " + buffer.length + " bytes");
		ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
		java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory
				.getInstance("X.509");
		Certificate cert = cf.generateCertificate(bis);
		// System.out.println("Certificate : " + cert);
		return (java.security.cert.X509Certificate) cert;
	}
}