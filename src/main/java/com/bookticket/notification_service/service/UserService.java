package com.bookticket.notification_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class UserService {
    private final RestClient userRestClient;

    public UserService(@Qualifier("userRestClient") RestClient userRestClient) {
        this.userRestClient = userRestClient;
    }

    public String getEmailById(Long userId) {
        return userRestClient.get()
                .uri("api/v1/users/{userId}/email", userId)
                .retrieve()
                .body(String.class);
    }
}
