package com.tp.snapseat.booking_service.client;

import com.tp.snapseat.booking_service.config.EventClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * REST client for the Event/Show Service. Only idempotent GETs, so bounded retry is safe (ST-17).
 * Timeouts come from spring.cloud.openfeign.client.config.default.*; circuit breaker + fallback
 * from spring.cloud.openfeign.circuitbreaker.enabled (ST-16 / ST-18).
 * An empty url means "resolve by service name" once discovery is introduced (ST-11).
 */
@FeignClient(
        name = "event-service",
        url = "${event.service.url:}",
        configuration = EventClientConfig.class,
        fallbackFactory = EventServiceFallbackFactory.class,
        dismiss404 = true
)
public interface EventServiceClient {

    /** Returns null when the show does not exist (404 is dismissed). */
    @GetMapping("/api/shows/{showId}")
    ShowResponse getShow(@PathVariable("showId") Long showId);

    /** Returns null / empty when the show does not exist. */
    @GetMapping("/api/shows/{showId}/seats") List<SeatLayoutResponse> getSeatLayout( @PathVariable("showId") Long showId);
}
