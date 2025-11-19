package com.bookticket.notification_service.exception;

/**
 * Exception for retryable notification failures
 * Thrown when a transient error occurs (5xx, network issues, timeouts)
 * Should NOT be thrown for client errors (4xx) as they won't succeed on retry
 */
public class RetryableNotificationException extends RuntimeException {
    
    private final String failureReason;
    
    public RetryableNotificationException(String message, String failureReason) {
        super(message);
        this.failureReason = failureReason;
    }
    
    public RetryableNotificationException(String message, String failureReason, Throwable cause) {
        super(message, cause);
        this.failureReason = failureReason;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
}

