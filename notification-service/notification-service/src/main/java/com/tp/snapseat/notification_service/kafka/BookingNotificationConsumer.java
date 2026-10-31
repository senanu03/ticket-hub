package com.tp.snapseat.notification_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tp.snapseat.notification_service.dto.BookingResponseDTO;
import com.tp.snapseat.notification_service.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingNotificationConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public BookingNotificationConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "booking.confirmed",
            groupId = "notification-service"
    )
    public void consume(ConsumerRecord<String, String> record) {

        try {

            BookingResponseDTO booking =
                    objectMapper.readValue(
                            record.value(),
                            BookingResponseDTO.class
                    );

            log.info(
                    "Received BookingConfirmed event. topic={}, bookingId={}",
                    record.topic(),
                    booking.getBookingId()
            );

            notificationService.processNotification(
                    record.topic(),
                    booking
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to process booking.confirmed event. value={}",
                    record.value(),
                    ex
            );

            throw new IllegalStateException(
                    "Invalid booking.confirmed Kafka event",
                    ex
            );
        }
    }
}
