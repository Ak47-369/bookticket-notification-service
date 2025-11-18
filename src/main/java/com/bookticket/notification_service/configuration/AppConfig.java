package com.bookticket.notification_service.configuration;

import com.bookticket.notification_service.security.HeaderPropagationInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    private final ServiceUrlProperties serviceUrlProperties;

    public AppConfig(ServiceUrlProperties serviceUrlProperties) {
        this.serviceUrlProperties = serviceUrlProperties;
    }

    @Bean
    public HeaderPropagationInterceptor headerPropagationInterceptor() {
        return new HeaderPropagationInterceptor();
    }

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean("theaterRestClient")
    public RestClient theaterRestClient(RestClient.Builder loadBalancedRestClientBuilder) {
        return loadBalancedRestClientBuilder
                .baseUrl(serviceUrlProperties.getTheaterUrl())
                .requestInterceptor(headerPropagationInterceptor())
                .build();
    }

    @Bean("bookingRestClient")
    public RestClient bookingRestClient(RestClient.Builder loadBalancedRestClientBuilder) {
        return loadBalancedRestClientBuilder
                .baseUrl(serviceUrlProperties.getBookingUrl())
                .requestInterceptor(headerPropagationInterceptor())
                .build();
    }

    @Bean("userRestClient")
    public RestClient userRestClient(RestClient.Builder loadBalancedRestClientBuilder) {
        return loadBalancedRestClientBuilder
                .baseUrl(serviceUrlProperties.getUserUrl())
                .requestInterceptor(headerPropagationInterceptor())
                .build();
    }
}
