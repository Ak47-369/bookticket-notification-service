package com.bookticket.notification_service.dto;

public record BookingFailedEvent(
        Long bookingId,
        Long userId,
        Long showId,
        Double totalAmount,
        String reason
) {
}
