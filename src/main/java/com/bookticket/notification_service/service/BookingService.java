package com.bookticket.notification_service.service;

import com.bookticket.notification_service.dto.SeatDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@Slf4j
public class BookingService {
    private final RestClient bookingRestClient;

    public BookingService(@Qualifier("bookingRestClient") RestClient bookingRestClient) {
        this.bookingRestClient = bookingRestClient;
    }

    public List<SeatDetails> getSeatDetails(Long bookingId) {
        return bookingRestClient.get()
                .uri("api/v1/bookings/{bookingId}/seats", bookingId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
