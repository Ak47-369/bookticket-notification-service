package com.bookticket.notification_service.service;

import com.bookticket.notification_service.dto.BookingFailedEvent;
import com.bookticket.notification_service.dto.BookingSuccessEvent;
import com.bookticket.notification_service.dto.SeatDetails;
import com.bookticket.notification_service.dto.ShowDetails;
import com.bookticket.notification_service.dto.TicketDetails;
import com.bookticket.notification_service.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NotificationOrchestrationService {

    private final EmailService emailService;
    private final PdfService pdfService;
    private final UserService userService;
    private final TheaterService theaterService;
    private final BookingService bookingService;
    private final EmailTemplateService emailTemplateService;

    public NotificationOrchestrationService(EmailService emailService, PdfService pdfService, UserService userService,
                                            TheaterService theaterService, BookingService bookingService,
                                            EmailTemplateService emailTemplateService) {
        this.emailService = emailService;
        this.pdfService = pdfService;
        this.userService = userService;
        this.theaterService = theaterService;
        this.bookingService = bookingService;
        this.emailTemplateService = emailTemplateService;
    }

    public void processBookingSuccess(BookingSuccessEvent bookingSuccessEvent) {
        log.info("Processing successful booking for user: {}", bookingSuccessEvent.userId());

        TicketDetails ticketDetails;
        try {
            UserContext.setUserId(bookingSuccessEvent.userId());
            String userEmail = userService.getEmailById(bookingSuccessEvent.userId());
            userEmail = "qanonymous756@gmail.com"; // TODO - Remove this later
            ShowDetails showDetails = theaterService.getShowDetails(bookingSuccessEvent.showId());
            List<SeatDetails> seatDetails = bookingService.getSeatDetails(bookingSuccessEvent.bookingId());
            ticketDetails = new TicketDetails(bookingSuccessEvent.bookingId(), bookingSuccessEvent.userId(), userEmail, showDetails, seatDetails);
        } catch (Exception e) {
            log.error("Failed to fetch ticket details for booking {}: {}", bookingSuccessEvent.bookingId(), e.getMessage());
            throw new RuntimeException("Failed to fetch ticket details", e);
        } finally {
            UserContext.clear();
        }

        byte[] pdfBytes = pdfService.generateTicketPdf(ticketDetails);
        String htmlBody = emailTemplateService.generateBookingSuccessEmail(ticketDetails);
        emailService.sendEmailWithAttachment(
                ticketDetails.emailId(),
                "🎉 Booking Confirmed - Your Tickets are Ready!",
                htmlBody,
                "ticket.pdf",
                pdfBytes
        );
        log.info("Successfully sent ticket to: {}", ticketDetails.emailId());
    }

    public void processBookingFailure(BookingFailedEvent bookingFailedEvent) {
        log.info("Processing failed booking for user: {}", bookingFailedEvent.userId());
        String userEmail = null;
        try {
            UserContext.setUserId(bookingFailedEvent.userId());
            userEmail = userService.getEmailById(bookingFailedEvent.userId());
            userEmail = "qanonymous756@gmail.com"; // TODO - Remove this later
            String htmlBody = emailTemplateService.generateBookingFailureEmail(
                    "Valued Customer",
                    bookingFailedEvent.bookingId(),
                    bookingFailedEvent.reason()
            );
            emailService.sendHtmlEmail(userEmail, "❌ Booking Unsuccessful - Please Try Again", htmlBody);
        } catch (Exception e) {
            log.error("Failed to fetch user email for user {}: error : {}", bookingFailedEvent.userId(), e);
            throw new RuntimeException("Failed to fetch user email", e);
        } finally {
            UserContext.clear();
        }
        log.info("Successfully sent failure notification to: {}", userEmail);
    }
}
