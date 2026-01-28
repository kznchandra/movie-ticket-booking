package com.pbs.bookingservice.entity;

import com.pbs.bookingservice.entity.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "SEAT_INVENTORY")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class SeatInventory implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double price;
    @Enumerated(EnumType.STRING)
    private SeatStatus seatStatus;
    private String seatNumber;

    @ManyToOne
    @JoinColumn(name = "SHOW_ID")
    private Show show;


}
