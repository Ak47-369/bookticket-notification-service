package com.bookticket.notification_service.service;

import com.bookticket.notification_service.dto.SeatDetails;
import com.bookticket.notification_service.dto.TicketDetails;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;

@Service
public class EmailTemplateService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    private final TemplateEngine templateEngine;

    public EmailTemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String generateBookingSuccessEmail(TicketDetails ticketDetails) {
        Context context = new Context();

        // Calculate total amount
        double totalAmount = ticketDetails.seats().stream()
                .mapToDouble(SeatDetails::price)
                .sum();

        // Format date and time
        String showDate = ticketDetails.showDetails().startTime().format(DATE_FORMATTER);
        String showTime = ticketDetails.showDetails().startTime().format(TIME_FORMATTER);

        // Set template variables
        context.setVariable("bookingId", ticketDetails.bookingId());
        context.setVariable("movieTitle", ticketDetails.showDetails().movieTitle());
        context.setVariable("showDate", showDate);
        context.setVariable("showTime", showTime);
        context.setVariable("theaterName", ticketDetails.showDetails().theaterName());
        context.setVariable("theaterAddress", ticketDetails.showDetails().theaterAddress());
        context.setVariable("screenName", ticketDetails.showDetails().screenName());
        context.setVariable("seats", ticketDetails.seats());
        context.setVariable("totalAmount", totalAmount);

        // Process template
        return templateEngine.process("email/booking-success", context);
    }

    public String generateBookingFailureEmail(String userName, Long bookingId, String reason) {
        Context context = new Context();

        // Set template variables
        context.setVariable("userName", userName != null ? userName : "there");
        context.setVariable("bookingId", bookingId != null ? bookingId : "N/A");
        context.setVariable("reason", reason != null ? reason : "Unknown error occurred");

        // Process template
        return templateEngine.process("email/booking-failure", context);
    }
}

