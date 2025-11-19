package com.bookticket.notification_service.service;

import com.bookticket.notification_service.dto.BookingFailedEvent;
import com.bookticket.notification_service.dto.BookingSuccessEvent;
import com.bookticket.notification_service.entity.FailedNotification;
import com.bookticket.notification_service.enums.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Scheduled service to automatically retry failed notifications from Dead Letter Queue
 * Runs every 5 minutes to process pending notifications
 */
@Service
@Slf4j
public class NotificationDLQRetryScheduler {
    
    private final NotificationDLQService dlqService;
    private final NotificationOrchestrationService orchestrationService;
    
    public NotificationDLQRetryScheduler(NotificationDLQService dlqService,
                                        NotificationOrchestrationService orchestrationService) {
        this.dlqService = dlqService;
        this.orchestrationService = orchestrationService;
    }
    
    /**
     * Scheduled job to retry failed notifications
     * Runs every 5 minutes (300000 ms)
     * Initial delay of 1 minute to allow application to fully start
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 60000)
    public void retryFailedNotifications() {
        log.info("Starting notification DLQ retry job...");
        
        List<FailedNotification> pendingNotifications = dlqService.getPendingNotifications();
        
        if (pendingNotifications.isEmpty()) {
            log.info("No pending notifications in DLQ to retry");
            return;
        }
        
        log.info("Found {} pending notifications in DLQ to retry", pendingNotifications.size());
        
        for (FailedNotification notification : pendingNotifications) {
            try {
                retryNotification(notification);
            } catch (Exception e) {
                log.error("Error retrying DLQ notification {}: {}", notification.getId(), e.getMessage(), e);
            }
        }
        
        log.info("Notification DLQ retry job completed");
    }
    
    /**
     * Retry a single failed notification
     */
    private void retryNotification(FailedNotification notification) {
        log.info("Retrying DLQ notification {} (type: {}, booking: {}, attempt: {}/{})",
                notification.getId(), notification.getNotificationType(), notification.getBookingId(), 
                notification.getRetryCount() + 1, notification.getMaxRetries());
        
        try {
            if (notification.getNotificationType() == NotificationType.BOOKING_SUCCESS) {
                retryBookingSuccessNotification(notification);
            } else if (notification.getNotificationType() == NotificationType.BOOKING_FAILED) {
                retryBookingFailureNotification(notification);
            }
            
            // If successful, mark as processed
            dlqService.markAsProcessed(notification.getId());
            log.info("Successfully processed DLQ notification {}", notification.getId());
            
        } catch (Exception e) {
            // Increment retry count and update error
            dlqService.incrementRetryCount(notification.getId(), e.getMessage());
            log.error("Failed to retry DLQ notification {}: {}", notification.getId(), e.getMessage());
        }
    }
    
    /**
     * Retry booking success notification
     * Note: This will NOT use the @Retryable mechanism as it's called directly
     * The orchestration service will handle the retry logic
     */
    private void retryBookingSuccessNotification(FailedNotification notification) {
        BookingSuccessEvent successEvent = new BookingSuccessEvent(
                notification.getBookingId(),
                notification.getUserId(),
                notification.getShowId(),
                notification.getTotalAmount()
        );
        
        // Call the orchestration service directly
        // Note: Since this is called from scheduler, it won't be async
        // We need to call it synchronously to catch exceptions
        orchestrationService.processBookingSuccess(successEvent);
    }
    
    /**
     * Retry booking failure notification
     */
    private void retryBookingFailureNotification(FailedNotification notification) {
        BookingFailedEvent failedEvent = new BookingFailedEvent(
                notification.getBookingId(),
                notification.getUserId(),
                notification.getShowId(),
                notification.getTotalAmount(),
                notification.getReason()
        );
        
        // Call the orchestration service directly
        orchestrationService.processBookingFailure(failedEvent);
    }
}

