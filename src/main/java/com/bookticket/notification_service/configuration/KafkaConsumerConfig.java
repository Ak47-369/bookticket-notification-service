package com.bookticket.notification_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Kafka Consumer Configuration with Exponential Backoff
 *
 * This configuration handles Kafka message consumption failures at the listener level.
 * Note: This is separate from Spring @Retryable which handles business logic failures.
 * Both layers work together:
 * 1. Kafka Consumer retries (this config) - for message consumption issues
 * 2. Spring Retry (@Retryable) - for business logic failures (REST calls, etc.)
 */
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler errorHandler() {
        ExponentialBackOff exponentialBackOff = new ExponentialBackOff();
        exponentialBackOff.setInitialInterval(1000L);      // Start with 1 second
        exponentialBackOff.setMultiplier(2.0);             // Double each time
        exponentialBackOff.setMaxInterval(10000L);         // Cap at 10 seconds
        exponentialBackOff.setMaxElapsedTime(15000L);      // Total retry window: 15 seconds

        return new DefaultErrorHandler(exponentialBackOff);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }
}
