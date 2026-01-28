package com.pbs.bookingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SHOW")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class Show implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "MOVIE_ID", nullable = false)
    private Long movieId;

    @Column(name = "THEATRE_ID", nullable = false)
    private Long theatreId;

    @Column(name = "SHOW_TIME", nullable = false)
    private LocalDateTime showTime;

    @Column(name = "END_TIME", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "TOTAL_SEATS", nullable = false)
    private Integer totalSeats;

    @Column(name = "AVAILABLE_SEATS", nullable = false)
    private Integer availableSeats;

    @Column(name = "BASE_PRICE", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "STATUS", nullable = false)
    private String status;


}
