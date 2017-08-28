package org.wso2telco.analytics.sparkUdf.exception;

/**
 * Created by isuru on 8/24/17.
 */
public class KillBillException extends Exception {

    public KillBillException() {
        super();
    }

    public KillBillException(String message) {
        super(message);
    }

    public KillBillException(String message, Throwable cause) {
        super(message, cause);
    }
}
