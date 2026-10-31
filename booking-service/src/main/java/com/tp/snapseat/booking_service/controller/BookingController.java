package com.tp.snapseat.booking_service.controller;

import com.tp.snapseat.booking_service.dto.BookingRequest;
import com.tp.snapseat.booking_service.dto.BookingResponse;
import com.tp.snapseat.booking_service.dto.PagedResponse;
import com.tp.snapseat.booking_service.security.CurrentUser;
import com.tp.snapseat.booking_service.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/** Customer-facing booking API. Customer identity always comes from the JWT. */
@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Create a booking and reserve seats",
            description = "Idempotent per Idempotency-Key: a retry returns the original booking. "
                    + "The booking is PENDING_PAYMENT until it is paid or expires.")
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        BookingResponse response = bookingService.createBooking(CurrentUser.from(authentication), idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, URI.create("/api/bookings/" + response.bookingId()).toString())
                .body(response);
    }

    @Operation(summary = "Get one of my bookings (status, seats, amount, expiry)")
    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(@PathVariable Long bookingId, Authentication authentication) {
        return bookingService.getBooking(CurrentUser.from(authentication), bookingId);
    }

    @Operation(summary = "List my bookings (newest first, paginated)")
    @GetMapping
    public PagedResponse<BookingResponse> listMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        return bookingService.listMyBookings(CurrentUser.from(authentication), page, size);
    }

    @Operation(summary = "Cancel my unpaid booking and release its seats",
            description = "Only PENDING_PAYMENT bookings can be cancelled. A CONFIRMED booking returns 409.")
    @PostMapping("/{bookingId}/cancel")
    public BookingResponse cancelBooking(@PathVariable Long bookingId, Authentication authentication) {
        return bookingService.cancelBooking(CurrentUser.from(authentication), bookingId);
    }
}
