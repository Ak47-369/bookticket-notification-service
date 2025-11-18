package com.bookticket.notification_service.dto;

import java.util.List;

public record TicketDetails(
        Long bookingId,
        Long userId,
        String emailId,
        ShowDetails showDetails,
        List<SeatDetails> seats
) {
}
