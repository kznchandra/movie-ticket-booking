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

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void saveBookingInitiatedEvent(Booking booking) {
        saveBookingEvent(booking, "BOOKING_INITIATED");
    }

    public void saveBookingConfirmedEvent(Booking booking) {
        saveBookingEvent(booking, "BOOKING_CONFIRMED");
    }

    public void saveBookingExpiredEvent(Booking booking) {
        saveBookingEvent(booking, "BOOKING_EXPIRED");
    }

    private void saveBookingEvent(Booking booking, String eventType) {
        OutboxEvent event = createOutboxEvent(booking, eventType);
        outboxRepository.save(event);
    }

    private OutboxEvent createOutboxEvent(Booking booking, String eventType) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("BOOKING");
        event.setAggregateId(booking.getBookingReference());
        event.setEventType(eventType);
        event.setStatus("NEW");
        try {
            event.setPayload(objectMapper.writeValueAsString(serializeBookingPayload(booking)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return event;
    }

    private Map<String, Object> serializeBookingPayload(Booking booking) {
        return Map.of(
                "bookingReference", booking.getBookingReference(),
                "userId", booking.getUserId(),
                "showId", booking.getShowId(),
                "amount", booking.getFinalAmount()
        );
    }
}