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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {


    private static final int BATCH_SIZE = 50;
    private static final int PAGE_NUMBER = 0;
    private static final String STATUS_SENT = "SENT";
    private static final String STATUS_FAILED = "FAILED";

    private static final String EVENT_TYPE_BOOKING_INITIATED = "BOOKING_INITIATED";
    private static final String EVENT_TYPE_BOOKING_CONFIRMED = "BOOKING_CONFIRMED";
    private static final String EVENT_TYPE_BOOKING_EXPIRED = "BOOKING_EXPIRED";

    private static final String TOPIC_BOOKING_INITIATED = "booking.initiated";
    private static final String TOPIC_BOOKING_CONFIRMED = "booking.confirmed";
    private static final String TOPIC_BOOKING_EXPIRED = "booking.expired";

    private static final Map<String, String> EVENT_TYPE_TO_TOPIC_MAP = java.util.Map.of(
            EVENT_TYPE_BOOKING_INITIATED, TOPIC_BOOKING_INITIATED,
            EVENT_TYPE_BOOKING_CONFIRMED, TOPIC_BOOKING_CONFIRMED,
            EVENT_TYPE_BOOKING_EXPIRED, TOPIC_BOOKING_EXPIRED
    );

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEvent> events = outboxRepository.findPendingEvents(
                PageRequest.of(PAGE_NUMBER, BATCH_SIZE)
        );

        if (events.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox events", events.size());

        for (OutboxEvent event : events) {
            processEvent(event);
        }

        outboxRepository.saveAll(events);
    }

    private void processEvent(OutboxEvent event) {
        try {
            String topic = resolveTopic(event.getEventType());
            kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload());

            event.setStatus(STATUS_SENT);
            event.setProcessedAt(LocalDateTime.now());

            log.info("Successfully published event {} to topic {}", event.getEventId(), topic);

        } catch (Exception ex) {
            log.error("Kafka publish failed for event {} with error: {}",
                    event.getEventId(), ex.getMessage(), ex);
            event.setStatus(STATUS_FAILED);
        }
    }

    private String resolveTopic(String eventType) {
        String topic = EVENT_TYPE_TO_TOPIC_MAP.get(eventType);
        if (topic == null) {
            throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
        return topic;
    }
}