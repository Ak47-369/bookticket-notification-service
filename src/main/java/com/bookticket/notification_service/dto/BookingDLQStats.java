package com.bookticket.notification_service.dto;

import com.bookticket.notification_service.entity.FailedNotification;

import java.util.List;

/**
     * DTO for booking-specific DLQ statistics
     */
public record BookingDLQStats(
            Long bookingId,
            int pendingCount,
            int retryingCount,
            int failedCount,
            int processedCount,
            List<FailedNotification> notifications
    ) {}