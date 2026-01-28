package com.pbs.bookingservice.service;

import com.pbs.bookingservice.entity.SeatInventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class OfferService {

    private static final String THIRD_TICKET_50_OFF = "THIRD_TICKET_50";
    private static final int MINIMUM_SEATS_FOR_THIRD_TICKET_OFFER = 3;
    private static final BigDecimal THIRD_TICKET_DISCOUNT_PERCENTAGE = BigDecimal.valueOf(0.5);

    public BigDecimal applyOffer(String offerCode, List<SeatInventory> seats) {
        if (seats == null || seats.isEmpty()) {
            return BigDecimal.ZERO;
        }

        if (THIRD_TICKET_50_OFF.equals(offerCode)) {
            return calculateThirdTicketDiscount(seats);
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal calculateThirdTicketDiscount(List<SeatInventory> seats) {
        if (seats.size() < MINIMUM_SEATS_FOR_THIRD_TICKET_OFFER) {
            return BigDecimal.ZERO;
        }

        BigDecimal thirdCheapestSeatPrice = getThirdCheapestSeatPrice(seats);
        return thirdCheapestSeatPrice.multiply(THIRD_TICKET_DISCOUNT_PERCENTAGE);
    }

    private BigDecimal getThirdCheapestSeatPrice(List<SeatInventory> seats) {
        return seats.stream()
                .sorted(Comparator.comparing(SeatInventory::getPrice))
                .skip(2)
                .findFirst()
                .map(seat -> BigDecimal.valueOf(seat.getPrice()))
                .orElse(BigDecimal.ZERO);
    }

}
