package com.bookticket.notification_service.dto;

public record BookingSuccessEvent(
        Long bookingId,
        Long userId,
        Long showId,
        Double totalAmount
) {
}
