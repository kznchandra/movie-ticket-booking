package com.pbs.bookingservice.service;

import com.pbs.bookingservice.common.req.OfferDiscountCode;
import com.pbs.bookingservice.entity.SeatInventory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class OfferService {

    private static final int MINIMUM_SEATS_FOR_THIRD_TICKET_OFFER = 3;
    private static final BigDecimal THIRD_TICKET_DISCOUNT_PERCENTAGE = BigDecimal.valueOf(0.5);

    public BigDecimal applyOffer(OfferDiscountCode offerCode, List<SeatInventory> seats) {
        if (!StringUtils.hasText(offerCode.name())) {
            return BigDecimal.ZERO;
        }
        log.debug("Applying offer code: {} for {} seats", offerCode, seats != null ? seats.size() : 0);
        if (offerCode.equals(OfferDiscountCode.THIRD_TICKET_50_DISCOUNT)) {
            BigDecimal discount = calculateThirdTicketDiscount(seats);
            log.info("Offer {} applied successfully. Discount: {}", offerCode, discount);
            return discount;
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal calculateThirdTicketDiscount(List<SeatInventory> seats) {
        log.debug("Calculating third ticket discount for {} seats", seats.size());

        if (seats.size() < MINIMUM_SEATS_FOR_THIRD_TICKET_OFFER) {
            log.info("Third ticket offer not applicable: Minimum {} seats required, but only {} provided",
                    MINIMUM_SEATS_FOR_THIRD_TICKET_OFFER, seats.size());
            return BigDecimal.ZERO;
        }

        BigDecimal thirdCheapestSeatPrice = getThirdCheapestSeatPrice(seats);
        BigDecimal discount = thirdCheapestSeatPrice.multiply(THIRD_TICKET_DISCOUNT_PERCENTAGE);
        log.info("Third ticket discount calculated: {} (50% of third cheapest seat price: {})",
                discount, thirdCheapestSeatPrice);
        return discount;
    }

    private BigDecimal getThirdCheapestSeatPrice(List<SeatInventory> seats) {
        log.debug("Finding third cheapest seat price from {} seats", seats.size());
        return seats.stream()
                .sorted(Comparator.comparing(SeatInventory::getPrice))
                .skip(2)
                .findFirst()
                .map(seat -> BigDecimal.valueOf(seat.getPrice()))
                .orElse(BigDecimal.ZERO);
    }

}
