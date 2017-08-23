package org.wso2telco.analytics.killbill.internal;

public class ReportEngineServiceConstants {
    public static final int SERVICE_MIN_THREAD_POOL_SIZE = 8;
    public static final int SERVICE_MAX_THREAD_POOL_SIZE = 100;
    public static final int SERVICE_EXECUTOR_JOB_QUEUE_SIZE = 2000;
    public static final long DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS = 20000;

    /**
     * To avoid instantiating
     */
    private ReportEngineServiceConstants() {
    }
}
