package com.tickethub.payment.event;

import com.tickethub.payment.entity.Payment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentEventPublisher(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Payment payment) {
        String topic = switch (payment.getStatus()) {
            case SUCCESS -> "tickethub.payment.succeeded.v1";
            case FAILED -> "tickethub.payment.failed.v1";
            default -> null;
        };

        if (topic == null) {
            return;
        }

        PaymentEvent event = new PaymentEvent(
                System.currentTimeMillis(),
                payment.getId(),
                payment.getBookingId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getProvider().name(),
                payment.getProviderReference(),
                Instant.now()
        );

        kafkaTemplate.send(topic, payment.getBookingId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        System.err.println("Payment event publish failed: " + ex.getMessage());
                    }
                });
    }
}
