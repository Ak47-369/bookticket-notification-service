package com.bookticket.notification_service.controller;

import com.bookticket.notification_service.configuration.swagger.InternalApi;
import com.bookticket.notification_service.dto.BookingFailedEvent;
import com.bookticket.notification_service.dto.BookingSuccessEvent;
import com.bookticket.notification_service.service.NotificationOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/internal/notifications")
@Tag(name = "Internal Notification Controller", description = "Internal APIs for triggering notifications")
public class EmailController {

    private final NotificationOrchestrationService orchestrationService;

    public EmailController(NotificationOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @InternalApi
    @Operation(
            summary = "Send Booking Success Email",
            description = "Receives a booking success event and orchestrates sending a confirmation email with a PDF ticket.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully processed booking success notification"),
                    @ApiResponse(responseCode = "500", description = "Failed to send booking success email")
            }
    )
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

    @InternalApi
    @Operation(
            summary = "Send Booking Failure Email",
            description = "Receives a booking failure event and orchestrates sending a notification email.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully processed booking failure notification"),
                    @ApiResponse(responseCode = "500", description = "Failed to send booking failure email")
            }
    )
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
