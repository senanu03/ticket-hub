package com.tp.snapseat.booking_service.service;

import com.tp.snapseat.booking_service.entity.ShowSeat;
import com.tp.snapseat.booking_service.repository.ShowSeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Seat layout belongs to Event/Show; Booking owns only reservation state. A reservation row is created lazily
 * the first time a seat is requested. Each insert runs in its own transaction so that a unique-constraint race
 * between two requests is harmless: the loser ignores it and the next step simply locks the winner's row.
 * Creating rows up-front keeps the later SELECT ... FOR UPDATE on existing rows only (no gap-lock deadlocks).
 */
@Component
public class SeatInitializer {

    private static final Logger log = LoggerFactory.getLogger(SeatInitializer.class);

    private final ShowSeatRepository showSeatRepository;
    private final TransactionTemplate requiresNew;

    public SeatInitializer(ShowSeatRepository showSeatRepository, PlatformTransactionManager transactionManager) {
        this.showSeatRepository = showSeatRepository;
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void ensureRows(Long showId, Collection<Long> seatIds) {
        Set<Long> missing = new HashSet<>(seatIds);
        missing.removeAll(showSeatRepository.findExistingSeatIds(showId, seatIds));
        for (Long seatId : missing) {
            try {
                requiresNew.executeWithoutResult(status ->
                        showSeatRepository.saveAndFlush(ShowSeat.available(showId, seatId)));
            } catch (DataAccessException ex) {
                log.debug("Seat row already created concurrently: showId={} seatId={}", showId, seatId);
            }
        }
    }
}
