package com.pbs.bookingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "BOOKING_SEAT")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class BookingSeat implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "BOOKING_ID")
    private Booking booking;
    private Long seatInventoryId;
    private Double price;
    private Double pricePaid;
}
