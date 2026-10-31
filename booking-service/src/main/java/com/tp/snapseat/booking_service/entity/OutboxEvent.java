package com.tp.snapseat.booking_service.entity;

import com.tp.snapseat.booking_service.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
})
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    @Column(name = "partition_key", length = 100)
    private String partitionKey;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    public void markPublished(Instant now) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = now;
        this.errorMessage = null;
    }

    public void markFailed(String error) {
        this.status = OutboxStatus.FAILED;
        this.errorMessage = error != null ? error.substring(0, Math.min(error.length(), 950)) : "Unknown failure";
        this.retryCount++;
    }
}
