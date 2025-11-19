package com.bookticket.notification_service.service;

import com.bookticket.notification_service.entity.FailedNotification;
import com.bookticket.notification_service.enums.NotificationStatus;
import com.bookticket.notification_service.enums.NotificationType;
import com.bookticket.notification_service.repository.FailedNotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service to manage Dead Letter Queue (DLQ) for failed notifications
 * Stores notifications that fail to process due to REST client errors
 */
@Service
@Slf4j
public class NotificationDLQService {
    
    private final FailedNotificationRepository failedNotificationRepository;
    
    public NotificationDLQService(FailedNotificationRepository failedNotificationRepository) {
        this.failedNotificationRepository = failedNotificationRepository;
    }
    
    /**
     * Store a failed booking success notification in DLQ
     */
    @Transactional
    public void storeFailedSuccessNotification(Long bookingId, Long userId, Long showId, 
                                               Double totalAmount, String failureReason, String errorMessage) {
        try {
            FailedNotification failedNotification = new FailedNotification();
            failedNotification.setNotificationType(NotificationType.BOOKING_SUCCESS);
            failedNotification.setBookingId(bookingId);
            failedNotification.setUserId(userId);
            failedNotification.setShowId(showId);
            failedNotification.setTotalAmount(totalAmount);
            failedNotification.setStatus(NotificationStatus.PENDING);
            failedNotification.setFailureReason(failureReason);
            failedNotification.setLastError(truncateError(errorMessage));
            
            // Store event as JSON for later replay
            String payload = String.format(
                "{\"bookingId\":%d,\"userId\":%d,\"showId\":%d,\"totalAmount\":%.2f}",
                bookingId, userId, showId, totalAmount
            );
            failedNotification.setEventPayload(payload);
            
            failedNotificationRepository.save(failedNotification);
            log.warn("Stored failed BOOKING_SUCCESS notification in DLQ for booking {}, reason: {}", 
                    bookingId, failureReason);
            
        } catch (Exception e) {
            log.error("Failed to store notification in DLQ for booking {}: {}", bookingId, e.getMessage(), e);
        }
    }
    
    /**
     * Store a failed booking failure notification in DLQ
     */
    @Transactional
    public void storeFailedFailureNotification(Long bookingId, Long userId, Long showId,
                                               Double totalAmount, String reason, String failureReason, String errorMessage) {
        try {
            FailedNotification failedNotification = new FailedNotification();
            failedNotification.setNotificationType(NotificationType.BOOKING_FAILED);
            failedNotification.setBookingId(bookingId);
            failedNotification.setUserId(userId);
            failedNotification.setShowId(showId);
            failedNotification.setTotalAmount(totalAmount);
            failedNotification.setReason(reason);
            failedNotification.setStatus(NotificationStatus.PENDING);
            failedNotification.setFailureReason(failureReason);
            failedNotification.setLastError(truncateError(errorMessage));

            // Store event as JSON for later replay
            String payload = String.format(
                "{\"bookingId\":%d,\"userId\":%d,\"showId\":%d,\"totalAmount\":%.2f,\"reason\":\"%s\"}",
                bookingId, userId, showId, totalAmount, reason != null ? reason.replace("\"", "\\\"") : ""
            );
            failedNotification.setEventPayload(payload);

            failedNotificationRepository.save(failedNotification);
            log.warn("Stored failed BOOKING_FAILED notification in DLQ for booking {}, reason: {}",
                    bookingId, failureReason);

        } catch (Exception e) {
            log.error("Failed to store notification in DLQ for booking {}: {}", bookingId, e.getMessage(), e);
        }
    }

    /**
     * Store a notification with permanent failure (non-retryable errors like 404, 403)
     * These are stored directly as FAILED status and won't be retried by the scheduler
     * This provides visibility and monitoring for permanent failures
     */
    @Transactional
    public void storeFailedNotificationAsPermanentFailure(NotificationType notificationType,
                                                          Long bookingId, Long userId, Long showId,
                                                          Double totalAmount, String reason,
                                                          String failureReason, String errorMessage) {
        try {
            FailedNotification failedNotification = new FailedNotification();
            failedNotification.setNotificationType(notificationType);
            failedNotification.setBookingId(bookingId);
            failedNotification.setUserId(userId);
            failedNotification.setShowId(showId);
            failedNotification.setTotalAmount(totalAmount);
            failedNotification.setReason(reason);
            failedNotification.setStatus(NotificationStatus.FAILED); // Directly mark as FAILED
            failedNotification.setRetryCount(0); // No retries attempted
            failedNotification.setMaxRetries(0); // No retries allowed
            failedNotification.setFailureReason(failureReason);
            failedNotification.setLastError(truncateError(errorMessage));

            // Store event as JSON for audit trail
            String payload;
            if (notificationType == NotificationType.BOOKING_SUCCESS) {
                payload = String.format(
                    "{\"bookingId\":%d,\"userId\":%d,\"showId\":%d,\"totalAmount\":%.2f}",
                    bookingId, userId, showId, totalAmount
                );
            } else {
                payload = String.format(
                    "{\"bookingId\":%d,\"userId\":%d,\"showId\":%d,\"totalAmount\":%.2f,\"reason\":\"%s\"}",
                    bookingId, userId, showId, totalAmount, reason != null ? reason.replace("\"", "\\\"") : ""
                );
            }
            failedNotification.setEventPayload(payload);

            failedNotificationRepository.save(failedNotification);
            log.warn("Stored {} notification as PERMANENT FAILURE in DLQ for booking {}, reason: {} ({})",
                    notificationType, bookingId, failureReason, errorMessage);

        } catch (Exception e) {
            log.error("Failed to store permanent failure in DLQ for booking {}: {}", bookingId, e.getMessage(), e);
        }
    }
    
    /**
     * Get all pending notifications that can be retried
     */
    public List<FailedNotification> getPendingNotifications() {
        return failedNotificationRepository.findByStatusAndRetryCountLessThan(NotificationStatus.PENDING, 3);
    }
    
    /**
     * Get all permanently failed notifications (exhausted retries)
     */
    public List<FailedNotification> getFailedNotifications() {
        return failedNotificationRepository.findByStatus(NotificationStatus.FAILED);
    }

    /**
     * Get all notifications for a specific booking ID
     */
    public List<FailedNotification> getNotificationsByBookingId(Long bookingId) {
        return failedNotificationRepository.findByBookingId(bookingId);
    }
    
    /**
     * Mark notification as successfully processed
     */
    @Transactional
    public void markAsProcessed(Long notificationId) {
        failedNotificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setStatus(NotificationStatus.PROCESSED);
            notification.setProcessedAt(LocalDateTime.now());
            failedNotificationRepository.save(notification);
            log.info("Marked DLQ notification {} as PROCESSED", notificationId);
        });
    }
    
    /**
     * Increment retry count and update last error
     */
    @Transactional
    public void incrementRetryCount(Long notificationId, String errorMessage) {
        failedNotificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setLastRetryAt(LocalDateTime.now());
            notification.setLastError(truncateError(errorMessage));
            
            // If max retries reached, mark as FAILED
            if (notification.getRetryCount() >= notification.getMaxRetries()) {
                notification.setStatus(NotificationStatus.FAILED);
                log.error("DLQ notification {} has exhausted all retries. Marking as FAILED", notificationId);
            }
            
            failedNotificationRepository.save(notification);
        });
    }
    
    /**
     * Truncate error message to fit in database column
     */
    private String truncateError(String error) {
        if (error == null) return null;
        return error.length() > 2000 ? error.substring(0, 2000) : error;
    }
}

