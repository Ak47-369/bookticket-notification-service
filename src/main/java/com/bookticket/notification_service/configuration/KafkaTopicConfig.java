package com.bookticket.notification_service.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Slf4j
public class KafkaTopicConfig {

    @Bean
    public NewTopic bookingSuccessTopic() {
        return TopicBuilder.name("booking_success")
                .build();
    }

    @Bean
    public NewTopic bookingFailedTopic() {
        return TopicBuilder.name("booking_failed")
                .build();
    }
}
