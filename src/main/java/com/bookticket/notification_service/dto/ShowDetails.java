package com.bookticket.notification_service.dto;

import java.time.LocalDateTime;

public record ShowDetails(
        Long showId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String movieId,
        String movieTitle,
        String theaterName,
        String theaterAddress,
        String screenName
) {
}
