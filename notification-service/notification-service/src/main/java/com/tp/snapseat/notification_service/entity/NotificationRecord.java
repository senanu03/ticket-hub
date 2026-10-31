package com.tp.snapseat.notification_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_records", indexes = {
        @Index(name = "idx_notification_booking", columnList = "booking_id"),
        @Index(name = "idx_notification_event_id", columnList = "event_id")
})
public class NotificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true, nullable = false, length = 100)
    private String eventId;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationStatus status;

    @Column(nullable = false, length = 50)
    private String channel;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public NotificationRecord() {}

    public NotificationRecord(Long id, String eventId, Long bookingId, String recipientEmail, String eventType,
                              NotificationStatus status, String channel, int retryCount, String errorMessage,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.eventId = eventId;
        this.bookingId = bookingId;
        this.recipientEmail = recipientEmail;
        this.eventType = eventType;
        this.status = status;
        this.channel = channel;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NotificationRecordBuilder builder() {
        return new NotificationRecordBuilder();
    }

    public static class NotificationRecordBuilder {
        private Long id;
        private String eventId;
        private Long bookingId;
        private String recipientEmail;
        private String eventType;
        private NotificationStatus status;
        private String channel;
        private int retryCount;
        private String errorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        NotificationRecordBuilder() {}

        public NotificationRecordBuilder id(Long id) { this.id = id; return this; }
        public NotificationRecordBuilder eventId(String eventId) { this.eventId = eventId; return this; }
        public NotificationRecordBuilder bookingId(Long bookingId) { this.bookingId = bookingId; return this; }
        public NotificationRecordBuilder recipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; return this; }
        public NotificationRecordBuilder eventType(String eventType) { this.eventType = eventType; return this; }
        public NotificationRecordBuilder status(NotificationStatus status) { this.status = status; return this; }
        public NotificationRecordBuilder channel(String channel) { this.channel = channel; return this; }
        public NotificationRecordBuilder retryCount(int retryCount) { this.retryCount = retryCount; return this; }
        public NotificationRecordBuilder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public NotificationRecordBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public NotificationRecordBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public NotificationRecord build() {
            return new NotificationRecord(id, eventId, bookingId, recipientEmail, eventType, status, channel, retryCount, errorMessage, createdAt, updatedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
