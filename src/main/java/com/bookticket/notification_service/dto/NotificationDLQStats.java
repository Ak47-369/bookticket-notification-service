package com.bookticket.notification_service.dto;

public record NotificationDLQStats(
        int pendingCount,
        int failedCount,
        int totalCount
) {}