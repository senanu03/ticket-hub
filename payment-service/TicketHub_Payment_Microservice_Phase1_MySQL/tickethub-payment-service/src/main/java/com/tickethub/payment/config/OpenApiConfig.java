package com.tickethub.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI ticketHubPaymentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TicketHub Payment Service API")
                        .version("v1")
                        .description("Payment initiation, status and Razorpay webhook processing."));
    }
}
