package com.pbs.bookingservice.kafka;

import com.pbs.bookingservice.entity.Booking;
import com.pbs.bookingservice.kafka.event.BookingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;



@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    public void bookingInitiated(Booking booking) {
        BookingEvent event = BookingEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("BOOKING_INITIATED")
                .eventTime(LocalDateTime.now())
                .booking(booking)
                .build();
        kafkaTemplate.send("booking-initiated", booking.getId().toString(), objectMapper.writeValueAsString(event));
    }

    public void bookingConfirmed(Booking booking) {
        BookingEvent event = BookingEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("BOOKING_CONFIRMED")
                .eventTime(LocalDateTime.now())
                .booking(booking)
                .build();
        kafkaTemplate.send("booking-confirmed", booking.getId().toString(), objectMapper.writeValueAsString(event));
    }

    public void bookingExpired(Booking booking) {
        BookingEvent event = BookingEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("BOOKING_EXPIRED")
                .eventTime(LocalDateTime.now())
                .booking(booking)
                .build();
        kafkaTemplate.send("booking-expired", booking.getId().toString(), objectMapper.writeValueAsString(event));
    }
}
