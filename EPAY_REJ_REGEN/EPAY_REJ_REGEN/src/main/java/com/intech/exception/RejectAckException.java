package com.intech.exception;

/**
 * Generic runtime exception for Reject ACK module.
 */
public class RejectAckException extends RuntimeException {

	public RejectAckException(String message) {
		super(message);
	}

	public RejectAckException(String message, Throwable cause) {
		super(message, cause);
	}
}
