package com.intech.exception;

/**
 * Thrown when processing of a single batch fails.
 */
public class BatchProcessingException extends RuntimeException {

	public BatchProcessingException(String message) {
		super(message);
	}

	public BatchProcessingException(String message, Throwable cause) {
		super(message, cause);
	}
}
