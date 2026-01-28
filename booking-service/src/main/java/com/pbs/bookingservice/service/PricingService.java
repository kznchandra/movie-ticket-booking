package com.pbs.bookingservice.service;

import com.pbs.bookingservice.common.resp.PricingDetails;
import com.pbs.bookingservice.entity.SeatInventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {
    
    private final OfferService offerService;
    public PricingDetails calculatePricing(
            List<SeatInventory> seats,
            String offerCode) {

        BigDecimal base =
                seats.stream()
                        .map(seat -> BigDecimal.valueOf(seat.getPrice()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;



        // Apply offer code discount
        if (offerCode != null && !offerCode.isEmpty()) {
            BigDecimal offerDiscount = offerService.applyOffer(base, offerCode, seats);
            discount = discount.add(offerDiscount);
        }

        return new PricingDetails(base, discount, base.subtract(discount));
    }

}
