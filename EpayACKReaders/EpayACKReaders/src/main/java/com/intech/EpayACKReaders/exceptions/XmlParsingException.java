// src/main/java/com/intech/epayackreader/exception/XmlParsingException.java
package com.intech.EpayACKReaders.exceptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class XmlParsingException extends RuntimeException {

	private static final Logger LOGGER = LogManager.getLogger(XmlParsingException.class);

	private final String filePath;

	public XmlParsingException(String message, String filePath) {
		super(message);
		this.filePath = filePath;
		LOGGER.error("XML parsing error for file {}: {}", filePath, message);
	}

	public XmlParsingException(String message, String filePath, Throwable cause) {
		super(message, cause);
		this.filePath = filePath;
		LOGGER.error("XML parsing error for file {}: {}", filePath, message, cause);
	}

	public String getFilePath() {
		return filePath;
	}
}
