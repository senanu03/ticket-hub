package com.tp.snapseat.notification_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Bean
    public CommonErrorHandler commonErrorHandler(KafkaOperations<Object, Object> kafkaOperations) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaOperations,
                (record, ex) -> {
                    log.error("Kafka record exhausted retries, routing to DLT: topic={}, key={}, error={}",
                            record.topic(), record.key(), ex.getMessage());
                    return new TopicPartition(record.topic() + ".DLT", record.partition());
                });

        // 3 retries with 1-second interval before publishing to DLT
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.warn("Retrying Kafka event delivery attempt {}: topic={}, partition={}, offset={}, reason={}",
                    deliveryAttempt, record.topic(), record.partition(), record.offset(), ex.getMessage());
        });

        return errorHandler;
    }
}
