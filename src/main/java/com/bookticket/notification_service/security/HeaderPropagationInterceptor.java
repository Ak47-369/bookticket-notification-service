package com.bookticket.notification_service.security;


import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

public class HeaderPropagationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String userId = null;
        String userRoles = null;
        String username = null;

        // Try to get from HTTP request context (for regular HTTP requests)
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            userId = attributes.getRequest().getHeader("X-User-Id");
            userRoles = attributes.getRequest().getHeader("X-User-Roles");
            username = attributes.getRequest().getHeader("X-User-Name");
        }

        // If not available from HTTP context, try UserContext (for Kafka events)
        if (userId == null) {
            Long userIdFromContext = UserContext.getUserId();
            if (userIdFromContext != null) {
                userId = String.valueOf(userIdFromContext);
            }
        }

        // For service-to-service communication, always use SERVICE_ACCOUNT role
        if (userRoles != null) {
            userRoles += ",SERVICE_ACCOUNT";
        } else {
            userRoles = "SERVICE_ACCOUNT";
        }

        // Add headers to the outgoing request
        if (userId != null) {
            request.getHeaders().set("X-User-Id", userId);
        }

        request.getHeaders().set("X-User-Roles", userRoles);
        if (username != null) {
            request.getHeaders().set("X-User-Name", username);
        }

        return execution.execute(request, body);
    }
}
