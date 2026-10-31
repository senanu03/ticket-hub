package com.tp.snapseat.booking_service.event;

import com.tp.snapseat.booking_service.config.BookingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publishes BookingConfirmed only AFTER the booking transaction commits, keyed by bookingId
 * (keeps per-booking ordering). A publish failure is logged loudly and never rolls back or
 * changes the confirmed booking; guaranteed publishing (outbox) is Phase 2 (ST-36).
 */
@Component
@ConditionalOnProperty(prefix = "booking.events", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BookingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookingEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public BookingEventPublisher(KafkaTemplate<String, String> bookingKafkaTemplate, BookingProperties properties) {
        this.kafkaTemplate = bookingKafkaTemplate;
        this.topic = properties.getEvents().getBookingConfirmedTopic();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        log.info(
                "BookingConfirmed listener INVOKED: bookingId={} eventId={} topic={}",
                event.bookingId(),
                event.eventId(),
                topic);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("bookingId", String.valueOf(event.bookingId()))) {
            kafkaTemplate.send(topic, String.valueOf(event.bookingId()), event.toJson())
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("BookingConfirmed publish FAILED: bookingId={} eventId={} topic={}",
                                    event.bookingId(), event.eventId(), topic, ex);
                        } else {
                            log.info("BookingConfirmed published: bookingId={} eventId={} topic={} partition={} offset={}",
                                    event.bookingId(), event.eventId(), topic,
                                    result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                        }
                    });
        } catch (RuntimeException ex) {
            log.error("BookingConfirmed publish FAILED: bookingId={} eventId={} topic={}",
                    event.bookingId(), event.eventId(), topic, ex);
        }
    }
}
