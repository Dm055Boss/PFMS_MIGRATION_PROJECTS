package com.intech.exception;

/**
 * Wraps database query failures with business context (which query, which account, etc.).
 */
public class DataFetchException extends RuntimeException {
    public DataFetchException(String message, Throwable cause) { super(message, cause); }
}
