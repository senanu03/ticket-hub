package com.tp.snapseat.booking_service.event;

import com.tp.snapseat.booking_service.entity.Booking;
import com.tp.snapseat.booking_service.util.JsonUtil;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * BookingConfirmed event contract (ST-19), version 1.
 * eventId is deterministic per booking, so a re-published event carries the same id and
 * the Notification Service can suppress duplicates (ST-58).
 */
public record BookingConfirmedEvent(
        String eventId,
        String eventType,
        int eventVersion,
        Long bookingId,
        String customerId,
        String customerEmail,
        Long showId,
        List<Long> seatIds,
        BigDecimal amount,
        String currency,
        Instant occurredAt
) {
    public static final String TYPE = "BookingConfirmed";

    public static BookingConfirmedEvent from(Booking booking, Instant occurredAt) {
        String id = UUID.nameUUIDFromBytes((TYPE + ":" + booking.getBookingId()).getBytes(StandardCharsets.UTF_8)).toString();
        return new BookingConfirmedEvent(id, TYPE, 1, booking.getBookingId(), booking.getCustomerId(),
                booking.getCustomerEmail(), booking.getShowId(), booking.seatIds(), booking.getAmount(),
                booking.getCurrency(), occurredAt);
    }

    public String toJson() {
        String seats = seatIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        return "{"
                + "\"eventId\":" + JsonUtil.quote(eventId)
                + ",\"eventType\":" + JsonUtil.quote(eventType)
                + ",\"eventVersion\":" + eventVersion
                + ",\"bookingId\":" + bookingId
                + ",\"customerId\":" + JsonUtil.quote(customerId)
                + ",\"customerEmail\":" + JsonUtil.quote(customerEmail)
                + ",\"showId\":" + showId
                + ",\"seatIds\":[" + seats + "]"
                + ",\"amount\":" + amount.toPlainString()
                + ",\"currency\":" + JsonUtil.quote(currency)
                + ",\"occurredAt\":" + JsonUtil.quote(occurredAt.toString())
                + "}";
    }
}
