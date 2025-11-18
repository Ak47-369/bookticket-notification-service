package com.bookticket.notification_service.dto;

public record SeatDetails(
        Long seatId,
        String seatNumber,
        String seatType,
        Double price
) {
}
