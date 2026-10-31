package com.tp.snapseat.notification_service.client;

import com.tp.snapseat.notification_service.dto.BookingResponseDTO;
import com.tp.snapseat.notification_service.security.ServiceTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BookingClient {

    private final RestClient restClient;
    private final ServiceTokenProvider serviceTokenProvider;

    public BookingClient(
            RestClient.Builder restClientBuilder,
            @Value("${booking-service.url:http://localhost:8084}") String bookingServiceUrl,
            ServiceTokenProvider serviceTokenProvider) {

        this.restClient = restClientBuilder
                .baseUrl(bookingServiceUrl)
                .build();

        this.serviceTokenProvider = serviceTokenProvider;
    }

    public BookingResponseDTO getBooking(Long bookingId) {

        return restClient.get()
                .uri("/api/bookings/{id}", bookingId)
                .headers(headers ->
                        headers.setBearerAuth(
                                serviceTokenProvider.createServiceToken()
                        ))
                .retrieve()
                .body(BookingResponseDTO.class);
    }
}
