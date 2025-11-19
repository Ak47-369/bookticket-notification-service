package com.bookticket.notification_service.entity;

import com.bookticket.notification_service.enums.NotificationStatus;
import com.bookticket.notification_service.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity to store failed notifications for Dead Letter Queue (DLQ)
 * When REST calls to fetch user/show/seat details fail, notifications are stored here for retry
 */
@Entity
@Table(name = "failed_notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FailedNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;
    
    @Column(nullable = false)
    private Long bookingId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Long showId;
    
    @Column(nullable = false)
    private Double totalAmount;
    
    @Column(length = 1000)
    private String reason;  // For BOOKING_FAILED notifications
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventPayload;  // JSON representation of the event
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;
    
    @Column(nullable = false)
    private Integer retryCount = 0;
    
    @Column(nullable = false)
    private Integer maxRetries = 3;
    
    @Column(length = 2000)
    private String lastError;  // Last error message
    
    @Column(length = 500)
    private String failureReason;  // What failed: USER_SERVICE, THEATER_SERVICE, BOOKING_SERVICE, EMAIL_SERVICE
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime lastRetryAt;
    
    @Column
    private LocalDateTime processedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

