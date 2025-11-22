package com.bookticket.notification_service.controller;

import com.bookticket.notification_service.dto.BookingDLQStats;
import com.bookticket.notification_service.dto.NotificationDLQStats;
import com.bookticket.notification_service.entity.FailedNotification;
import com.bookticket.notification_service.enums.NotificationStatus;
import com.bookticket.notification_service.service.NotificationDLQService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/notification-dlq")
@Slf4j
@Tag(name = "Admin - Notification DLQ", description = "APIs for managing the Dead Letter Queue for notifications")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class NotificationDLQController {
    
    private final NotificationDLQService dlqService;
    
    public NotificationDLQController(NotificationDLQService dlqService) {
        this.dlqService = dlqService;
    }

    @Operation(
            summary = "Get pending notifications",
            description = "Retrieves a list of all notifications in the DLQ with PENDING status that are awaiting retry attempts.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved pending notifications",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedNotification.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role"),
                    @ApiResponse(responseCode = "500", description = "Internal server error"),
                    @ApiResponse(responseCode = "503", description = "Service unavailable"),
                    @ApiResponse(responseCode = "504", description = "Gateway timeout"),
                    @ApiResponse(responseCode = "429", description = "Too many requests")
            }
    )
    @GetMapping("/pending")
    public ResponseEntity<List<FailedNotification>> getPendingNotifications() {
        log.info("Admin request: Get pending DLQ notifications");
        List<FailedNotification> notifications = dlqService.getPendingNotifications();
        return ResponseEntity.ok(notifications);
    }

    @Operation(
            summary = "Get permanently failed notifications",
            description = "Retrieves a list of all notifications that have exhausted all retry attempts and are marked as FAILED.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved failed notifications",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FailedNotification.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role"),
                    @ApiResponse(responseCode = "500", description = "Internal server error"),
                    @ApiResponse(responseCode = "503", description = "Service unavailable"),
                    @ApiResponse(responseCode = "504", description = "Gateway timeout"),
                    @ApiResponse(responseCode = "429", description = "Too many requests")
            }
    )
    @GetMapping("/failed")
    public ResponseEntity<List<FailedNotification>> getFailedNotifications() {
        log.info("Admin request: Get permanently failed DLQ notifications");
        List<FailedNotification> notifications = dlqService.getFailedNotifications();
        return ResponseEntity.ok(notifications);
    }

    @Operation(
            summary = "Mark a notification as processed",
            description = "Manually marks a DLQ notification as PROCESSED. This is an administrative action to be used after an issue has been resolved manually.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notification successfully marked as processed"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role"),
                    @ApiResponse(responseCode = "404", description = "Notification not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error"),
                    @ApiResponse(responseCode = "503", description = "Service unavailable"),
                    @ApiResponse(responseCode = "504", description = "Gateway timeout"),
                    @ApiResponse(responseCode = "429", description = "Too many requests")
            }
    )
    @PostMapping("/{notificationId}/mark-processed")
    public ResponseEntity<String> markAsProcessed(
            @Parameter(description = "ID of the DLQ notification to mark as processed", required = true)
            @PathVariable Long notificationId) {
        log.info("Admin request: Mark DLQ notification {} as processed", notificationId);
        dlqService.markAsProcessed(notificationId);
        return ResponseEntity.ok("Notification marked as processed");
    }

    @Operation(
            summary = "Get overall DLQ statistics",
            description = "Retrieves a count of all pending, failed, and total events in the notification DLQ.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved DLQ statistics",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationDLQStats.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role"),
                    @ApiResponse(responseCode = "500", description = "Internal server error"),
                    @ApiResponse(responseCode = "503", description = "Service unavailable"),
                    @ApiResponse(responseCode = "504", description = "Gateway timeout"),
                    @ApiResponse(responseCode = "429", description = "Too many requests")
            }
    )
    @GetMapping("/stats")
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

    @Operation(
            summary = "Get DLQ statistics by booking ID",
            description = "Returns a summary and list of all failed notifications associated with a specific booking ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved DLQ stats for the booking",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDLQStats.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role"),
                    @ApiResponse(responseCode = "500", description = "Internal server error"),
                    @ApiResponse(responseCode = "503", description = "Service unavailable"),
                    @ApiResponse(responseCode = "504", description = "Gateway timeout"),
                    @ApiResponse(responseCode = "429", description = "Too many requests")
            }
    )
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<BookingDLQStats> getStatsByBookingId(
            @Parameter(description = "ID of the booking to retrieve DLQ stats for", required = true)
            @PathVariable Long bookingId) {
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
