package com.bookticket.notification_service.repository;

import com.bookticket.notification_service.entity.FailedNotification;
import com.bookticket.notification_service.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FailedNotificationRepository extends JpaRepository<FailedNotification, Long> {
    
    /**
     * Find all notifications that are pending and haven't exceeded max retries
     */
    List<FailedNotification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetries);
    
    /**
     * Find all failed notifications for a specific booking
     */
    List<FailedNotification> findByBookingId(Long bookingId);
    
    /**
     * Find all notifications with FAILED status (exhausted retries)
     */
    List<FailedNotification> findByStatus(NotificationStatus status);
}

