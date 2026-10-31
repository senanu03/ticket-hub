package com.tp.snapseat.booking_service.service;

import com.tp.snapseat.booking_service.client.EventServiceClient;
import com.tp.snapseat.booking_service.client.SeatLayoutResponse;
import com.tp.snapseat.booking_service.client.ShowResponse;
import com.tp.snapseat.booking_service.dto.BookingRequest;
import com.tp.snapseat.booking_service.dto.BookingResponse;
import com.tp.snapseat.booking_service.enums.SeatStatus;
import com.tp.snapseat.booking_service.exception.BookingException;
import com.tp.snapseat.booking_service.repository.BookingRepository;
import com.tp.snapseat.booking_service.repository.ShowSeatRepository;
import com.tp.snapseat.booking_service.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * ST-08: proves with a real database (H2) and real transactions that, of N simultaneous requests for the same
 * seats, exactly one wins and the rest get a controlled conflict. Also proves SC-13 under a concurrent retry.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:bookingit;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "booking.events.enabled=false",
        "tickethub.security.jwk-set-uri=http://localhost:9/jwks",
        "tickethub.security.issuer-uri="
})
class SeatConcurrencyIT {

    private static final Long SHOW_ID = 77L;

    @Autowired BookingService bookingService;
    @Autowired BookingRepository bookingRepository;
    @Autowired ShowSeatRepository showSeatRepository;
    @MockitoBean EventServiceClient eventServiceClient;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        showSeatRepository.deleteAll();
        when(eventServiceClient.getShow(SHOW_ID)).thenReturn(new ShowResponse(SHOW_ID, 5L, "PUBLISHED", 9L,
                java.time.LocalDateTime.now().plusHours(1), java.time.LocalDateTime.now().plusHours(3),
                "OPEN", 1, 10, 1L, java.time.LocalDateTime.now()));
        when(eventServiceClient.getSeatLayout(SHOW_ID)).thenReturn(List.of(
                new SeatLayoutResponse(1L, "A", 1, "STD"), new SeatLayoutResponse(2L, "A", 2, "STD")));
    }

    @Test
    void ofManySimultaneousRequestsForTheSameSeats_exactlyOneSucceeds() throws Exception {
        int threads = 8;
        List<Callable<BookingResponse>> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            CurrentUser customer = new CurrentUser("customer-" + i, null, false);
            String key = "key-" + i;
            tasks.add(() -> bookingService.createBooking(customer, key, new BookingRequest(SHOW_ID, List.of(1L, 2L))));
        }

        List<Object> outcomes = runConcurrently(tasks);

        long successes = outcomes.stream().filter(o -> o instanceof BookingResponse).count();
        long controlledFailures = outcomes.stream().filter(o -> o instanceof BookingException).count();
        assertThat(successes).isEqualTo(1);
        assertThat(controlledFailures).isEqualTo(threads - 1);
        assertThat(bookingRepository.count()).isEqualTo(1);
        assertThat(showSeatRepository.findAll()).hasSize(2)
                .allMatch(s -> s.getStatus() == SeatStatus.RESERVED);
    }

    @Test
    void concurrentRetriesWithTheSameIdempotencyKey_createOnlyOneBooking() throws Exception {
        CurrentUser customer = new CurrentUser("customer-1", null, false);
        List<Callable<BookingResponse>> tasks = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            tasks.add(() -> bookingService.createBooking(customer, "same-key", new BookingRequest(SHOW_ID, List.of(1L))));
        }

        List<Object> outcomes = runConcurrently(tasks);

        assertThat(outcomes).allMatch(o -> o instanceof BookingResponse);
        assertThat(outcomes.stream().map(o -> ((BookingResponse) o).bookingId()).distinct().count()).isEqualTo(1);
        assertThat(bookingRepository.count()).isEqualTo(1);
    }

    private <T> List<Object> runConcurrently(List<Callable<T>> tasks) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Object>> futures = new ArrayList<>();
        for (Callable<T> task : tasks) {
            futures.add(pool.submit(() -> {
                start.await();
                try {
                    return task.call();
                } catch (Exception ex) {
                    return ex;
                }
            }));
        }
        start.countDown();
        List<Object> results = new ArrayList<>();
        for (Future<Object> f : futures) {
            results.add(f.get(30, TimeUnit.SECONDS));
        }
        pool.shutdownNow();
        return results;
    }
}
