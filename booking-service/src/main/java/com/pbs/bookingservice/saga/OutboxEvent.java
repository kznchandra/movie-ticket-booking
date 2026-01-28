package com.pbs.bookingservice.saga;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    private String aggregateType;   // BOOKING
    private String aggregateId;     // bookingReference
    private String eventType;       // BOOKING_INITIATED

    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxEventStatus eventStatus;

    private String status;          // NEW, SENT, FAILED

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime processedAt;

}
