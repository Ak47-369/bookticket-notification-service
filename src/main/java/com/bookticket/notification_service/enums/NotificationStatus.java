package com.bookticket.notification_service.enums;

/**
 * Status of failed notifications in DLQ
 */
public enum NotificationStatus {
    PENDING,      // Waiting to be retried
    RETRYING,     // Currently being retried
    FAILED,       // Permanently failed after max retries
    PROCESSED     // Successfully processed
}

