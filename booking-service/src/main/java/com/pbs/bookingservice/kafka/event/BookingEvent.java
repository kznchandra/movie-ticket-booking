package com.pbs.bookingservice.kafka.event;

import com.pbs.bookingservice.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime eventTime;
    private Booking booking;
}
