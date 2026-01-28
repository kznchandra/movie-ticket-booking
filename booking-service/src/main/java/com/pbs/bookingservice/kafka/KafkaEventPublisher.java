package com.pbs.bookingservice.kafka;

import com.pbs.bookingservice.entity.Booking;
import com.pbs.bookingservice.kafka.event.BookingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;



@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private static final String TOPIC_BOOKING_INITIATED = "booking-initiated";
    private static final String TOPIC_BOOKING_CONFIRMED = "booking-confirmed";
    private static final String TOPIC_BOOKING_EXPIRED = "booking-expired";

    private enum EventType {
        BOOKING_INITIATED,
        BOOKING_CONFIRMED,
        BOOKING_EXPIRED
    }

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    public void bookingInitiated(Booking booking) {
        publishEvent(booking, EventType.BOOKING_INITIATED, TOPIC_BOOKING_INITIATED);
    }

    public void bookingConfirmed(Booking booking) {
        publishEvent(booking, EventType.BOOKING_CONFIRMED, TOPIC_BOOKING_CONFIRMED);
    }

    public void bookingExpired(Booking booking) {
        publishEvent(booking, EventType.BOOKING_EXPIRED, TOPIC_BOOKING_EXPIRED);
    }

    private void publishEvent(Booking booking, EventType eventType, String topic) {
        BookingEvent event = BookingEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType.name())
                .eventTime(LocalDateTime.now())
                .booking(booking)
                .build();

        String eventPayload = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(topic, booking.getId().toString(), eventPayload);

        log.debug("Published event {} for booking {}", eventType, booking.getId());
    }
}
