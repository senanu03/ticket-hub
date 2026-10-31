package com.tp.snapseat.event_service.repository;

import com.tp.snapseat.event_service.entity.Show;
import com.tp.snapseat.event_service.enums.ShowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("""
        SELECT COUNT(s) > 0
        FROM Show s
        WHERE s.screen.id = :screenId
          AND s.startTime < :endTime
          AND s.endTime > :startTime
          AND s.status <> com.tp.snapseat.event_service.enums.ShowStatus.CANCELLED
    """)
    boolean existsOverlappingShow(
            @Param("screenId") Long screenId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
        SELECT COUNT(s) > 0
        FROM Show s
        WHERE s.screen.id = :screenId
          AND s.startTime < :endTime
          AND s.endTime > :startTime
          AND s.id <> :showId
          AND s.status <> com.tp.snapseat.event_service.enums.ShowStatus.CANCELLED
    """)
    boolean existsOverlappingShowForUpdate(
            @Param("showId") Long showId,
            @Param("screenId") Long screenId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    Page<Show> findByEventId(Long eventId, Pageable pageable);

    Page<Show> findByEventIdAndStatusIn(Long eventId, Collection<ShowStatus> statuses, Pageable pageable);

    Page<Show> findByStatusIn(Collection<ShowStatus> statuses, Pageable pageable);
}
