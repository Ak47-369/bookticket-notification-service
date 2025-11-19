package com.bookticket.notification_service.exception;

/**
 * Exception for non-retryable notification failures
 * Thrown when a permanent error occurs (4xx client errors like 404, 403)
 * These should NOT be retried as they will always fail
 */
public class NonRetryableNotificationException extends RuntimeException {
    
    private final String failureReason;
    
    public NonRetryableNotificationException(String message, String failureReason) {
        super(message);
        this.failureReason = failureReason;
    }
    
    public NonRetryableNotificationException(String message, String failureReason, Throwable cause) {
        super(message, cause);
        this.failureReason = failureReason;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
}

