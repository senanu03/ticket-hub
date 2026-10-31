package com.tp.snapseat.notification_service.service;

import com.tp.snapseat.notification_service.dto.BookingResponseDTO;
import com.tp.snapseat.notification_service.entity.NotificationRecord;
import com.tp.snapseat.notification_service.entity.NotificationStatus;
import com.tp.snapseat.notification_service.repository.NotificationRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EmailService emailService;
    private final NotificationRecordRepository recordRepository;

    public NotificationService(EmailService emailService, NotificationRecordRepository recordRepository) {
        this.emailService = emailService;
        this.recordRepository = recordRepository;
    }

    @Value("${notification.email.to:shivarajamurugan007@gmail.com}")
    private String defaultNotificationEmail;

    @Transactional
    public void processNotification(String topic, BookingResponseDTO booking) {

        if (booking == null) {
            log.error("Booking data is missing");
            return;
        }

        String eventId = booking.getEventId();
        if (eventId == null || eventId.isBlank()) {
            eventId = "booking-" + booking.getBookingId() + "-" + topic;
        }

        // Duplicate suppression: if event has already been recorded, ignore
        if (recordRepository.existsByEventId(eventId)) {
            log.info("Duplicate notification suppressed: eventId={}, bookingId={}", eventId, booking.getBookingId());
            return;
        }

        // Send to actual customer's email address if present, else fallback to default
        String recipientEmail = (booking.getCustomerEmail() != null && !booking.getCustomerEmail().isBlank())
                ? booking.getCustomerEmail()
                : defaultNotificationEmail;

        NotificationRecord record = NotificationRecord.builder()
                .eventId(eventId)
                .bookingId(booking.getBookingId() != null ? booking.getBookingId() : 0L)
                .recipientEmail(recipientEmail)
                .eventType(topic)
                .channel("EMAIL")
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        record = recordRepository.save(record);

        try {
            switch (topic) {
                case "booking.created":
                    sendCreatedNotification(booking, recipientEmail);
                    break;

                case "booking.confirmed":
                    sendConfirmedNotification(booking, recipientEmail);
                    break;

                case "booking.failed":
                    sendFailedNotification(booking, recipientEmail);
                    break;

                case "booking.cancelled":
                    sendCancelledNotification(booking, recipientEmail);
                    break;

                default:
                    log.warn("Unknown Kafka topic: {}", topic);
            }

            record.setStatus(NotificationStatus.SENT);
            recordRepository.save(record);
            log.info("Notification successfully delivered: eventId={}, bookingId={}, recipient={}",
                    eventId, booking.getBookingId(), recipientEmail);

        } catch (Exception ex) {
            record.setStatus(NotificationStatus.FAILED);
            record.setErrorMessage(ex.getMessage() != null
                    ? ex.getMessage().substring(0, Math.min(ex.getMessage().length(), 950))
                    : "Unknown delivery error");
            record.setRetryCount(record.getRetryCount() + 1);
            recordRepository.save(record);

            log.error("Notification delivery failed: eventId={}, bookingId={}",
                    eventId, booking.getBookingId(), ex);
            throw ex;
        }
    }

    private void sendCreatedNotification(BookingResponseDTO booking, String to) {
        log.info("Sending booking created email. bookingId={}, to={}", booking.getBookingId(), to);
        emailService.sendBookingCreatedEmail(
                to,
                booking.getBookingId(),
                booking.getCustomerId(),
                booking.getAmount()
        );
    }

    private void sendConfirmedNotification(BookingResponseDTO booking, String to) {
        log.info("Sending booking confirmation email. bookingId={}, to={}", booking.getBookingId(), to);
        emailService.sendBookingConfirmedEmail(
                to,
                booking.getBookingId(),
                booking.getCustomerId(),
                booking.getAmount()
        );
    }

    private void sendFailedNotification(BookingResponseDTO booking, String to) {
        log.info("Sending booking failed email. bookingId={}, to={}", booking.getBookingId(), to);
        emailService.sendBookingFailedEmail(
                to,
                booking.getBookingId(),
                booking.getCustomerId()
        );
    }

    private void sendCancelledNotification(BookingResponseDTO booking, String to) {
        log.info("Sending booking cancellation email. bookingId={}, to={}", booking.getBookingId(), to);
        emailService.sendBookingCancelledEmail(
                to,
                booking.getBookingId(),
                booking.getCustomerId()
        );
    }
}
