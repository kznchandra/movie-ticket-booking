package com.pbs.bookingservice.saga;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishOutboxEvents() {

        List<OutboxEvent> events =
                outboxRepository.findPendingEvents(PageRequest.of(0, 50));

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(
                        resolveTopic(event.getEventType()),
                        event.getAggregateId(),
                        event.getPayload()
                );

                event.setStatus("SENT");
                event.setProcessedAt(LocalDateTime.now());

            } catch (Exception ex) {
                log.error("Kafka publish failed for event {}", event.getEventId(), ex);
                event.setStatus("FAILED");
            }
        }
    }

    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "BOOKING_INITIATED" -> "booking.initiated";
            case "BOOKING_CONFIRMED" -> "booking.confirmed";
            case "BOOKING_EXPIRED" -> "booking.expired";
            default -> throw new IllegalArgumentException("Unknown event type");
        };
    }
}