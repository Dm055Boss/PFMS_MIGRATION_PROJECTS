

// src/main/java/com/intech/epayackreader/exception/AckProcessingException.java
package com.intech.EpayACKReaders.exceptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AckProcessingException extends RuntimeException {

	private static final Logger LOGGER = LogManager.getLogger(AckProcessingException.class);

	public AckProcessingException(String message) {
		super(message);
		LOGGER.error("ACK processing error: {}", message);
	}

	public AckProcessingException(String message, Throwable cause) {
		super(message, cause);
		LOGGER.error("ACK processing error: {}", message, cause);
	}
}
