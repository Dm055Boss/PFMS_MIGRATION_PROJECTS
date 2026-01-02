package com.intech.exception;

/**
 * Thrown when required configuration is missing or invalid.
 */
public class ConfigException extends RuntimeException {
    public ConfigException(String message) { super(message); }
    public ConfigException(String message, Throwable cause) { super(message, cause); }
}
