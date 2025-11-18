package com.bookticket.notification_service.service;

import com.bookticket.notification_service.dto.BookingFailedEvent;
import com.bookticket.notification_service.dto.BookingSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BookingListener {

    private final NotificationOrchestrationService orchestrationService;

    public BookingListener(NotificationOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @KafkaListener(topics = "booking_success", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleBookingSuccess(BookingSuccessEvent bookingSuccessEvent) {
        log.info("Received Kafka message for successful booking: {}", bookingSuccessEvent);
        orchestrationService.processBookingSuccess(bookingSuccessEvent);
    }

    @KafkaListener(topics = "booking_failed", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleBookingFailed(BookingFailedEvent bookingFailedEvent) {
        log.info("Received Kafka message for failed booking: {}", bookingFailedEvent);
        orchestrationService.processBookingFailure(bookingFailedEvent);
    }
}
