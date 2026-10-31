package com.tp.snapseat.notification_service.repository;

import com.tp.snapseat.notification_service.entity.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRecordRepository extends JpaRepository<NotificationRecord, Long> {

    Optional<NotificationRecord> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    List<NotificationRecord> findByBookingId(Long bookingId);
}
