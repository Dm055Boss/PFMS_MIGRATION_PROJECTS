package com.intech.exception;

/**
 * Raised when XML generation or file writing fails.
 */
public class XmlWriteException extends RuntimeException {
    public XmlWriteException(String message, Throwable cause) { super(message, cause); }
}
