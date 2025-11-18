package com.bookticket.notification_service.service;

import com.bookticket.notification_service.dto.ShowDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class TheaterService {
    private final RestClient theaterRestClient;

    public TheaterService(@Qualifier("theaterRestClient") RestClient theaterRestClient) {
        this.theaterRestClient = theaterRestClient;
    }

    public ShowDetails getShowDetails(Long showId) {
        return theaterRestClient.get()
                .uri("api/v1/shows/{showId}", showId)
                .retrieve()
                .body(ShowDetails.class);
    }
}
