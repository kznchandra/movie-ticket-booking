package com.pbs.bookingservice.service;

import com.pbs.bookingservice.common.ex.SeatUnavailableException;
import com.pbs.bookingservice.common.resp.PricingDetails;
import com.pbs.bookingservice.entity.SeatInventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {
    
    private final OfferService offerService;

    public PricingDetails calculatePricing(
            List<SeatInventory> seats,
            String offerCode) {

        if (seats == null || seats.isEmpty()) {
            throw new SeatUnavailableException("Seats list cannot be null or empty");
        }

        BigDecimal basePrice = calculateBasePrice(seats);
        BigDecimal discount = calculateDiscount(offerCode, seats);
        BigDecimal finalPrice = basePrice.subtract(discount);

        return new PricingDetails(basePrice, discount, finalPrice);
    }

    private BigDecimal calculateBasePrice(List<SeatInventory> seats) {
        return seats.stream()
                .map(seat -> BigDecimal.valueOf(seat.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(String offerCode, List<SeatInventory> seats) {
        if (!StringUtils.hasText(offerCode)) {
            return BigDecimal.ZERO;
        }

        return offerService.applyOffer(offerCode, seats);
    }

}
