package com.tp.snapseat.booking_service.client;

import com.tp.snapseat.booking_service.exception.BookingException;
import com.tp.snapseat.booking_service.exception.DependencyUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * There is no meaningful substitute for show/price data, so the fallback fails fast with a controlled 503
 * (SC-05 / SC-16) instead of letting raw Feign / circuit-breaker errors leak out.
 */
@Component
public class EventServiceFallbackFactory implements FallbackFactory<EventServiceClient> {

    private static final Logger log = LoggerFactory.getLogger(EventServiceFallbackFactory.class);

    @Override
    public EventServiceClient create(Throwable cause) {
        return new EventServiceClient() {
            @Override
            public ShowResponse getShow(Long showId) {
                throw unavailable("getShow", cause);
            }

            @Override
            public List<SeatLayoutResponse> getSeatLayout(Long showId) {
                throw unavailable("getSeatLayout", cause);
            }
        };
    }

    private RuntimeException unavailable(String operation, Throwable cause) {
        if (cause instanceof BookingException be) {
            return be;
        }
        log.warn("Event service call failed: operation={} cause={}", operation, cause.toString());
        return new DependencyUnavailableException(
                "Event service is currently unavailable, please try again shortly", cause);
    }
}
