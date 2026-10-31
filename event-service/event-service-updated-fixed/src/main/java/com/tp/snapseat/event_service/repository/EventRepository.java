package com.tp.snapseat.event_service.repository;

import com.tp.snapseat.event_service.entity.Event;
import com.tp.snapseat.event_service.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    Page<Event> findByStatusAndTitleContainingIgnoreCase(
            EventStatus status,
            String search,
            Pageable pageable
    );
}
