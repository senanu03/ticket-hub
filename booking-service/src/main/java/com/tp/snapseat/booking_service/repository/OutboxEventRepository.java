package com.tp.snapseat.booking_service.repository;

import com.tp.snapseat.booking_service.entity.OutboxEvent;
import com.tp.snapseat.booking_service.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status AND o.retryCount < 5 ORDER BY o.createdAt ASC")
    List<OutboxEvent> findPendingEvents(@Param("status") OutboxStatus status);
}
