package com.tp.snapseat.booking_service.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/** Producer for BookingConfirmed. Bounded timeouts so a Kafka outage never hangs a payment-result call. */
@Configuration
@ConditionalOnProperty(prefix = "booking.events", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, String> bookingProducerFactory(
            @Value("${booking.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 3000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 8000);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> bookingKafkaTemplate(ProducerFactory<String, String> bookingProducerFactory) {
        return new KafkaTemplate<>(bookingProducerFactory);
    }
}
