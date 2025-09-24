package com.fadhli.automation.service;

/**
 * Exception class for Service layer operations
 * Wraps DAO and business logic related exceptions with meaningful error messages
 */
public class ServiceException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor
     */
    public ServiceException() {
        super();
    }
    
    /**
     * Constructor with message
     * @param message the error message
     */
    public ServiceException(String message) {
        super(message);
    }
    
    /**
     * Constructor with cause
     * @param cause the root cause exception
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Constructor with message and cause
     * @param message the error message
     * @param cause the root cause exception
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor with all parameters
     * @param message the error message
     * @param cause the root cause exception
     * @param enableSuppression whether to enable suppression
     * @param writableStackTrace whether to make stack trace writable
     */
    public ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}