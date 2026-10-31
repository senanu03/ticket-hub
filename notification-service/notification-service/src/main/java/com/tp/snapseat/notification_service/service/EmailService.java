package com.tp.snapseat.notification_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${notification.mail.simulate:false}")
    private boolean simulateMail;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    private void deliverEmail(SimpleMailMessage message) {
        if (simulateMail || mailUsername == null || mailUsername.isBlank()) {
            log.info("[SIMULATED EMAIL] To: {} | Subject: {} | Content:\n{}",
                    message.getTo(), message.getSubject(), message.getText());
            return;
        }
        mailSender.send(message);
    }

    public void sendBookingCreatedEmail(
            String to,
            Long bookingId,
            String customerId,
            BigDecimal amount) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("TicketHub - Booking Created");

        message.setText(
                "Hello,\n\n" +
                        "Your TicketHub booking has been created.\n\n" +
                        "Booking ID : " + bookingId + "\n" +
                        "Customer ID: " + customerId + "\n" +
                        "Amount     : ₹" + amount + "\n\n" +
                        "Your booking is currently being processed.\n\n" +
                        "Thank you,\n" +
                        "TicketHub Team"
        );

        deliverEmail(message);

        log.info(
                "Booking created email dispatched. bookingId={}, email={}",
                bookingId,
                to
        );
    }

    public void sendBookingConfirmedEmail(
            String to,
            Long bookingId,
            String customerId,
            BigDecimal amount) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("TicketHub - Booking Confirmed");

        message.setText(
                "Hello,\n\n" +
                        "Your TicketHub booking has been confirmed successfully!\n\n" +
                        "Booking ID : " + bookingId + "\n" +
                        "Customer ID: " + customerId + "\n" +
                        "Amount     : ₹" + amount + "\n\n" +
                        "Status     : CONFIRMED\n\n" +
                        "Enjoy your show!\n\n" +
                        "Thank you,\n" +
                        "TicketHub Team"
        );

        deliverEmail(message);

        log.info(
                "Booking confirmation email dispatched. bookingId={}, email={}",
                bookingId,
                to
        );
    }

    public void sendBookingFailedEmail(
            String to,
            Long bookingId,
            String customerId) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("TicketHub - Booking Failed");

        message.setText(
                "Hello,\n\n" +
                        "Unfortunately, your TicketHub booking could not be completed.\n\n" +
                        "Booking ID : " + bookingId + "\n" +
                        "Customer ID: " + customerId + "\n" +
                        "Status     : FAILED\n\n" +
                        "Please try booking again.\n\n" +
                        "Thank you,\n" +
                        "TicketHub Team"
        );

        deliverEmail(message);

        log.info(
                "Booking failed email dispatched. bookingId={}, email={}",
                bookingId,
                to
        );
    }

    public void sendBookingCancelledEmail(
            String to,
            Long bookingId,
            String customerId) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("TicketHub - Booking Cancelled");

        message.setText(
                "Hello,\n\n" +
                        "Your TicketHub booking has been cancelled.\n\n" +
                        "Booking ID : " + bookingId + "\n" +
                        "Customer ID: " + customerId + "\n" +
                        "Status     : CANCELLED\n\n" +
                        "Thank you,\n" +
                        "TicketHub Team"
        );

        deliverEmail(message);

        log.info(
                "Booking cancellation email dispatched. bookingId={}, email={}",
                bookingId,
                to
        );
    }
}
