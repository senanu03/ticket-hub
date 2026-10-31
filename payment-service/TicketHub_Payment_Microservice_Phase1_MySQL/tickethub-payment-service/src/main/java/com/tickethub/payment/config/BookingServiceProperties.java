package com.tickethub.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "booking-service")
public record BookingServiceProperties(
        String baseUrl,
        long connectTimeoutMs,
        long readTimeoutMs
) {}
