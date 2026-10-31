package com.tickethub.payment.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayClientConfig {

    @Bean
    RazorpayClient razorpayClient(RazorpayProperties properties) throws RazorpayException {
        if (properties.keyId() == null || properties.keyId().isBlank()
                || properties.keySecret() == null || properties.keySecret().isBlank()) {
            throw new IllegalStateException("Razorpay credentials are not configured");
        }
        return new RazorpayClient(properties.keyId(), properties.keySecret());
    }
}
