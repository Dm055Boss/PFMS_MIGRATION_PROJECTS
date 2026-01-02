// util/FilenameRules.java
package com.intech.cpsms.util;

public final class FilenameRules {
	private FilenameRules() {
	}

	/** Insert "ACK" immediately after "PAYREQ" token. */
	public static String responseMessageIdFromRequest(String requestMessageId) {
		int idx = requestMessageId.indexOf("PAYREQ");
		if (idx < 0)
			return requestMessageId + "ACK"; // fallback
		int insertPos = idx + "PAYREQ".length();
		return requestMessageId.substring(0, insertPos) + "ACK" + requestMessageId.substring(insertPos);
	}
}
