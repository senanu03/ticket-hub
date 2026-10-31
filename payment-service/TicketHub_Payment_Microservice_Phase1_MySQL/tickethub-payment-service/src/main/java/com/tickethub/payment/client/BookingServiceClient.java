package com.tickethub.payment.client;

import com.tickethub.payment.dto.BookingPaymentDetails;
import com.tickethub.payment.dto.PaymentResultRequest;
import com.tickethub.payment.exception.BookingServiceException;
import com.tickethub.payment.security.ServiceTokenProvider;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BookingServiceClient {

    private final RestClient restClient;
    private final ServiceTokenProvider serviceTokenProvider;

    public BookingServiceClient(
            RestClient bookingRestClient,
            ServiceTokenProvider serviceTokenProvider) {
        this.restClient = bookingRestClient;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    public BookingPaymentDetails getPaymentDetails(Long bookingId) {
        try {
            return restClient.get()
                    .uri("/internal/bookings/{bookingId}/payment-details", bookingId)
                    .headers(headers ->
                            headers.setBearerAuth(
                                    serviceTokenProvider.createServiceToken()))
                    .retrieve()
                    .body(BookingPaymentDetails.class);

        } catch (RestClientException ex) {
            throw new BookingServiceException(
                    "Booking Service could not validate booking " + bookingId,
                    ex);
        }
    }

    @Retry(name = "bookingService")
    @CircuitBreaker(
            name = "bookingService",
            fallbackMethod = "paymentResultFallback"
    )
    public void sendPaymentResult(
            Long bookingId,
            PaymentResultRequest request) {

        try {
            restClient.post()
                    .uri(
                            "/internal/bookings/{bookingId}/payment-result",
                            bookingId
                    )
                    .headers(headers ->
                            headers.setBearerAuth(
                                    serviceTokenProvider.createServiceToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException ex) {
            throw new BookingServiceException(
                    "Booking Service payment-result update failed",
                    ex);
        }
    }

    private void paymentResultFallback(
            Long bookingId,
            PaymentResultRequest request,
            Throwable ex) {

        // Payment state is already durable. Phase 1 deliberately exposes
        // the failure instead of pretending distributed compensation is complete.
        throw new BookingServiceException(
                "Booking update is temporarily unavailable",
                ex);
    }
}