package com.tp.snapseat.booking_service.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;

/** Deliberately NOT @Configuration: it applies only to EventServiceClient (via @FeignClient(configuration=...)). */
public class EventClientConfig {

    /** Bounded retry with backoff: 3 attempts, 200ms growing to at most 1s. Only network-level failures are retried. */
    @Bean
    public Retryer eventServiceRetryer() {
        return new Retryer.Default(200, 1000, 3);
    }
}
