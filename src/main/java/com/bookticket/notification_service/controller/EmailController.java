package com.bookticket.notification_service.controller;

import com.bookticket.notification_service.dto.BookingFailedEvent;
import com.bookticket.notification_service.dto.BookingSuccessEvent;
import com.bookticket.notification_service.service.NotificationOrchestrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/internal/notifications")
public class EmailController {

    private final NotificationOrchestrationService orchestrationService;

    public EmailController(NotificationOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @PostMapping("/booking-success")
    public ResponseEntity<String> sendBookingSuccessEmail(@RequestBody BookingSuccessEvent bookingSuccessEvent) {
        try {
            orchestrationService.processBookingSuccess(bookingSuccessEvent);
            return ResponseEntity.ok("Successfully processed booking success notification.");
        } catch (Exception e) {
            // Log the full error for debugging
            return ResponseEntity.status(500).body("Failed to send booking success email: " + e.getMessage());
        }
    }

    @PostMapping("/booking-failure")
    public ResponseEntity<String> sendBookingFailureEmail(@RequestBody BookingFailedEvent bookingFailedEvent) {
        try {
            orchestrationService.processBookingFailure(bookingFailedEvent);
            return ResponseEntity.ok("Successfully processed booking failure notification.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send booking failure email: " + e.getMessage());
        }
    }
}
