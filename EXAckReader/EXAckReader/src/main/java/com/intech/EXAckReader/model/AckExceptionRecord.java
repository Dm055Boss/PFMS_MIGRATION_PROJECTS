package com.intech.EXAckReader.model;

/**
 * One Exception record inside the Exceptions XML file. NOTE: OriginalMessageId
 * + AccountNumber is the matching key in DB.
 */
public record AckExceptionRecord(String originalMessageId, String messageId, String accountNumber, String remarks,
		String reconciliationType) {
	/**
	 * Normalizes OriginalMessageId by removing trailing .xml (case-insensitive),
	 * because DB TXNREQFN may store with or without .xml.
	 */
	public String normalizedOriginalMessageId() {
		if (originalMessageId == null)
			return null;
		String v = originalMessageId.trim();
		if (v.toLowerCase().endsWith(".xml")) {
			return v.substring(0, v.length() - 4);
		}
		return v;
	}

	/**
	 * Cleans remarks a bit (your sample had leading '|').
	 */
	public String cleanedRemarks() {
		if (remarks == null)
			return null;
		String r = remarks.trim();
		if (r.startsWith("|")) {
			r = r.substring(1).trim();
		}
		return r;
	}
}
