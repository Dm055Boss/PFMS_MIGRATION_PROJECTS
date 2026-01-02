package com.intech.regenSuc.util;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

/**
 * Small utility to marshal JAXB objects to XML files. Central place if you want
 * to tune encoding, formatting, etc.
 */
public final class XmlMarshallerUtil {

	private static final Logger log = LogManager.getLogger(XmlMarshallerUtil.class);

	private XmlMarshallerUtil() {
		// utility class
	}

	/**
	 * Marshal a JAXB-annotated object to a file.
	 *
	 * @param jaxbObject JAXB object instance (e.g. SuccessPayments)
	 * @param jaxbClass  Class of the object (e.g. SuccessPayments.class)
	 * @param file       Output XML file
	 * @throws Exception if marshalling fails
	 */
	public static <T> void marshalToFile(T jaxbObject, Class<T> jaxbClass, File file) throws Exception {
		if (jaxbObject == null) {
			throw new IllegalArgumentException("jaxbObject cannot be null");
		}
		if (jaxbClass == null) {
			throw new IllegalArgumentException("jaxbClass cannot be null");
		}
		if (file == null) {
			throw new IllegalArgumentException("output file cannot be null");
		}

		log.debug("Marshalling {} to file {}", jaxbClass.getSimpleName(), file.getAbsolutePath());

		JAXBContext ctx = JAXBContext.newInstance(jaxbClass);
		Marshaller marshaller = ctx.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		// If CPSMS strictly needs UTF-8 declaration, this is the default; you can set
		// explicitly:
		// marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

		marshaller.marshal(jaxbObject, file);
	}
}
