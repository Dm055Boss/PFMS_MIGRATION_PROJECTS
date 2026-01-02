package com.intech.exception;

/**
 * Thrown when a required SQL query key is missing from the external queries file.
 */
public class QueryNotFoundException extends RuntimeException {
    public QueryNotFoundException(String message) { super(message); }
}
