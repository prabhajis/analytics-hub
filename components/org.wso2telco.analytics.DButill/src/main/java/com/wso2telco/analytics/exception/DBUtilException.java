package com.wso2telco.analytics.exception;

public class DBUtilException extends Exception {

    /**
     * Instantiates a new db util exception.
     *
     * @param message the message
     */
    public DBUtilException(String message) {
        super(message);
    }

    /**
     * Instantiates a new db util exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public DBUtilException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new db util exception.
     *
     * @param cause the cause
     */
    public DBUtilException(Throwable cause) {
        super(cause);
    }
}
