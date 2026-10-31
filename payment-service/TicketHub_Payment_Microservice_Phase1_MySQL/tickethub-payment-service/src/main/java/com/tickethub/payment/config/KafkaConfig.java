package com.tickethub.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    NewTopic paymentSucceededTopic() {
        return TopicBuilder.name("tickethub.payment.succeeded.v1")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic paymentFailedTopic() {
        return TopicBuilder.name("tickethub.payment.failed.v1")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
