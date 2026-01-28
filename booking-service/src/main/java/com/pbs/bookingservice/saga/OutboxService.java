package com.pbs.bookingservice.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbs.bookingservice.entity.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private static final String AGGREGATE_TYPE_BOOKING = "BOOKING";
    private static final String EVENT_TYPE_BOOKING_INITIATED = "BOOKING_INITIATED";
    private static final String EVENT_TYPE_BOOKING_CONFIRMED = "BOOKING_CONFIRMED";
    private static final String EVENT_TYPE_BOOKING_EXPIRED = "BOOKING_EXPIRED";

    private static final String PAYLOAD_KEY_BOOKING_REFERENCE = "bookingReference";
    private static final String PAYLOAD_KEY_USER_ID = "userId";
    private static final String PAYLOAD_KEY_SHOW_ID = "showId";
    private static final String PAYLOAD_KEY_AMOUNT = "amount";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void saveBookingInitiatedEvent(Booking booking) {
        saveBookingEvent(booking, EVENT_TYPE_BOOKING_INITIATED);
    }

    public void saveBookingConfirmedEvent(Booking booking) {
        saveBookingEvent(booking, EVENT_TYPE_BOOKING_CONFIRMED);
    }

    public void saveBookingExpiredEvent(Booking booking) {
        saveBookingEvent(booking, EVENT_TYPE_BOOKING_EXPIRED);
    }

    private void saveBookingEvent(Booking booking, String eventType) {
        OutboxEvent event = createOutboxEvent(booking, eventType);
        outboxRepository.save(event);
    }

    private OutboxEvent createOutboxEvent(Booking booking, String eventType) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(AGGREGATE_TYPE_BOOKING);
        event.setAggregateId(booking.getBookingReference());
        event.setEventType(eventType);
        event.setEventStatus(OutboxEventStatus.PENDING);
        event.setStatus("NEW");
        try {
            event.setPayload(objectMapper.writeValueAsString(serializeBookingPayload(booking)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize booking payload for event type: " + eventType, e);
        }
        return event;
    }

    private Map<String, Object> serializeBookingPayload(Booking booking) {
        return Map.of(
                PAYLOAD_KEY_BOOKING_REFERENCE, booking.getBookingReference(),
                PAYLOAD_KEY_USER_ID, booking.getUserId(),
                PAYLOAD_KEY_SHOW_ID, booking.getShowId(),
                PAYLOAD_KEY_AMOUNT, booking.getFinalAmount()
        );
    }
}