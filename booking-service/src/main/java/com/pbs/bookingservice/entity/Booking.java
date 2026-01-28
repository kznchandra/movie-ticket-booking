package com.pbs.bookingservice.entity;

import com.pbs.bookingservice.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "BOOKING")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class Booking implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "BOOKING_REFERENCE", unique = true, nullable = false)
    private String bookingReference;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "SHOW_ID", nullable = false)
    private Long showId;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private BookingStatus status;

    @Column(name = "BASE_AMOUNT")
    private java.math.BigDecimal baseAmount;

    @Column(name = "DISCOUNT_AMOUNT")
    private java.math.BigDecimal discountAmount;

    @Column(name = "FINAL_AMOUNT")
    private java.math.BigDecimal finalAmount;

    @Column(name = "BOOKING_TIME")
    private java.time.LocalDateTime bookingTime;

    @Column(name = "EXPIRY_TIME")
    private java.time.LocalDateTime expiryTime;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingSeat> bookingSeats;


}
