package com.bookticket.notification_service.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services")
@Data
public class ServiceUrlProperties {
    
    private String theaterUrl;
    private String bookingUrl;
    private String userUrl;
}

