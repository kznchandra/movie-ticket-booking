package com.pbs.bookingservice.service;

import com.pbs.bookingservice.entity.SeatInventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OfferService {


    public BigDecimal applyOffer(BigDecimal baseAmount, String offerCodel, List<SeatInventory> seats) {
        BigDecimal discountAmount = BigDecimal.ZERO;
        // Third ticket 50% off
        if (seats.size() >= 3 && offerCodel.equals("THIRD_TICKET_50")) {
            BigDecimal third =
                    seats.stream()
                            .sorted(Comparator.comparing(SeatInventory::getPrice))
                            .skip(2)
                            .findFirst()
                            .map(seat -> BigDecimal.valueOf(seat.getPrice()))
                            .orElse(BigDecimal.ZERO);

            discountAmount = third.multiply(BigDecimal.valueOf(0.5));
        }
        return discountAmount;
    }

}
