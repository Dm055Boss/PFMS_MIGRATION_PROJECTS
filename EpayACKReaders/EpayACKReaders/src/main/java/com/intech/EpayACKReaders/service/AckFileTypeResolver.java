// src/main/java/com/intech/epayackreader/service/AckFileTypeResolver.java
package com.intech.EpayACKReaders.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

/**
 * Helper to detect file type from MsgNm or file name. You can tune patterns
 * according to actual PFMS naming (INIPAYACK, SUCPAYACK, REJPAYACK).
 */
public final class AckFileTypeResolver {

	private static final Logger LOGGER = LogManager.getLogger(AckFileTypeResolver.class);

	private AckFileTypeResolver() {
	}

	public static AckFileType detect(String msgNm, String fileName) {
		String source = (StringUtils.hasText(msgNm) ? msgNm : fileName);
		if (source == null) {
			throw new IllegalArgumentException("Cannot detect ACK file type: both msgNm and fileName are null.");
		}

		String upper = source.toUpperCase();

		if (upper.contains("INIPAY") || upper.contains("INIPAYACK") ) {
			return AckFileType.INI;
		} else if (upper.contains("SUCPAY") || upper.contains("SUCPAYACK") ) {
			return AckFileType.SUC;
		} else if (upper.contains("REJPAY") || upper.contains("REJPAYACK") ) {
			return AckFileType.REJ;
		}

		LOGGER.warn("Unable to detect ACK file type from msgNm='{}', fileName='{}'", msgNm,
				fileName);
		return AckFileType.SUC; // safe default, adjust if required
	}
}
