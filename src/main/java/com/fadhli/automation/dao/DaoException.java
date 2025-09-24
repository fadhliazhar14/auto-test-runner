package com.fadhli.automation.dao;

/**
 * Exception class for DAO operations
 * Wraps database-related exceptions with meaningful error messages
 */
public class DaoException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor
     */
    public DaoException() {
        super();
    }
    
    /**
     * Constructor with message
     * @param message the error message
     */
    public DaoException(String message) {
        super(message);
    }
    
    /**
     * Constructor with cause
     * @param cause the root cause exception
     */
    public DaoException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Constructor with message and cause
     * @param message the error message
     * @param cause the root cause exception
     */
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor with all parameters
     * @param message the error message
     * @param cause the root cause exception
     * @param enableSuppression whether to enable suppression
     * @param writableStackTrace whether to make stack trace writable
     */
    public DaoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}