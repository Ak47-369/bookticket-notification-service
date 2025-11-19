package com.bookticket.notification_service.controller;

import com.bookticket.notification_service.dto.BookingDLQStats;
import com.bookticket.notification_service.dto.NotificationDLQStats;
import com.bookticket.notification_service.entity.FailedNotification;
import com.bookticket.notification_service.enums.NotificationStatus;
import com.bookticket.notification_service.service.NotificationDLQService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing Dead Letter Queue for notifications
 * Provides endpoints to view and manage failed notifications
 */
@RestController
@RequestMapping("/api/v1/admin/notification-dlq")
@Slf4j
public class NotificationDLQController {
    
    private final NotificationDLQService dlqService;
    
    public NotificationDLQController(NotificationDLQService dlqService) {
        this.dlqService = dlqService;
    }
    
    /**
     * Get all pending notifications in DLQ
     * These are notifications that can still be retried
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FailedNotification>> getPendingNotifications() {
        log.info("Admin request: Get pending DLQ notifications");
        List<FailedNotification> notifications = dlqService.getPendingNotifications();
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Get all permanently failed notifications
     * These have exhausted all retry attempts
     */
    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FailedNotification>> getFailedNotifications() {
        log.info("Admin request: Get permanently failed DLQ notifications");
        List<FailedNotification> notifications = dlqService.getFailedNotifications();
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Manually mark a notification as processed
     * Use this after manually fixing the issue
     */
    @PostMapping("/{notificationId}/mark-processed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> markAsProcessed(@PathVariable Long notificationId) {
        log.info("Admin request: Mark DLQ notification {} as processed", notificationId);
        dlqService.markAsProcessed(notificationId);
        return ResponseEntity.ok("Notification marked as processed");
    }
    
    /**
     * Get notification statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDLQStats> getStats() {
        log.info("Admin request: Get notification DLQ statistics");

        List<FailedNotification> pending = dlqService.getPendingNotifications();
        List<FailedNotification> failed = dlqService.getFailedNotifications();

        NotificationDLQStats stats = new NotificationDLQStats(
                pending.size(),
                failed.size(),
                pending.size() + failed.size()
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * Get DLQ statistics for a specific booking ID
     * Returns all failed notifications and their statuses for the given booking
     */
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingDLQStats> getStatsByBookingId(@PathVariable Long bookingId) {
        log.info("Admin request: Get DLQ statistics for booking {}", bookingId);

        List<FailedNotification> notifications = dlqService.getNotificationsByBookingId(bookingId);

        if (notifications.isEmpty()) {
            return ResponseEntity.ok(new BookingDLQStats(
                    bookingId,
                    0,
                    0,
                    0,
                    0,
                    notifications
            ));
        }

        long pendingCount = notifications.stream()
                .filter(n -> n.getStatus() == NotificationStatus.PENDING)
                .count();

        long retryingCount = notifications.stream()
                .filter(n -> n.getStatus() == NotificationStatus.RETRYING)
                .count();

        long failedCount = notifications.stream()
                .filter(n -> n.getStatus() == NotificationStatus.FAILED)
                .count();

        long processedCount = notifications.stream()
                .filter(n -> n.getStatus() == NotificationStatus.PROCESSED)
                .count();

        BookingDLQStats stats = new BookingDLQStats(
                bookingId,
                (int) pendingCount,
                (int) retryingCount,
                (int) failedCount,
                (int) processedCount,
                notifications
        );

        return ResponseEntity.ok(stats);
    }
}

