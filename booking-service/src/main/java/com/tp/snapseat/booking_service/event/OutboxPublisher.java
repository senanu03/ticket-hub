package com.tp.snapseat.booking_service.event;

import com.tp.snapseat.booking_service.entity.OutboxEvent;
import com.tp.snapseat.booking_service.enums.OutboxStatus;
import com.tp.snapseat.booking_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "booking.events", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${booking.outbox.poll-interval-ms:2000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findPendingEvents(OutboxStatus.PENDING);
        if (pending.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getPartitionKey(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Outbox Kafka send failed for outboxId={}, eventType={}: {}",
                                        event.getId(), event.getEventType(), ex.getMessage());
                            } else {
                                log.info("Outbox event published to Kafka: outboxId={}, topic={}, offset={}",
                                        event.getId(), event.getTopic(), result.getRecordMetadata().offset());
                            }
                        });

                event.markPublished(now);
                outboxEventRepository.save(event);

            } catch (Exception ex) {
                log.warn("Failed to publish outbox event id={}: {}", event.getId(), ex.getMessage());
                event.markFailed(ex.getMessage());
                outboxEventRepository.save(event);
            }
        }
    }
}
