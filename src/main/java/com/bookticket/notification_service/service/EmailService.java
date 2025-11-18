package com.bookticket.notification_service.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentName, byte[] attachment) {
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML content
            helper.addAttachment(attachmentName, new ByteArrayResource(attachment));
            mailSender.send(message);
            log.info("Email sent to: {}",to);
        } catch (Exception e) {
            log.error("Email failed: {} ",e.getMessage());
            log.error("Failed to send email to: {} ",to);
            throw new RuntimeException("Failed to send email with attachment: " + e.getMessage(), e);
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML content
            mailSender.send(message);
            log.info("HTML email sent to: {}",to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to: {} ",to);
            throw new RuntimeException("Failed to send HTML email: " + e.getMessage(), e);
        }
    }
}
