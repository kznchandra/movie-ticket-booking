package com.pbs.bookingservice.service;

import com.pbs.bookingservice.common.ex.SeatUnavailableException;
import com.pbs.bookingservice.common.req.OfferDiscountCode;
import com.pbs.bookingservice.common.resp.PricingDetails;
import com.pbs.bookingservice.entity.SeatInventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingService {

    private final OfferService offerService;


    public PricingDetails calculatePricing(
            List<SeatInventory> seats,
            OfferDiscountCode offerCode) {

        log.debug("Calculating pricing for {} seats with offer code: {}",
                seats != null ? seats.size() : 0, offerCode);

        if (seats == null || seats.isEmpty()) {
            log.error("Pricing calculation failed: Seats list is null or empty");
            throw new SeatUnavailableException("Seats list cannot be null or empty");
        }

        BigDecimal basePrice = calculateBasePrice(seats);
        log.info("Base price calculated: {} for {} seats", basePrice, seats.size());

        BigDecimal discount = calculateDiscount(offerCode, seats);
        log.debug("Discount calculated: {} for offer code: {}", discount, offerCode);

        BigDecimal finalPrice = basePrice.subtract(discount);
        log.info("Final pricing calculated - Base: {}, Discount: {}, Final: {}",
                basePrice, discount, finalPrice);

        return new PricingDetails(basePrice, discount, finalPrice.subtract(discount));
    }

    private BigDecimal calculateBasePrice(List<SeatInventory> seats) {
        return seats.stream()
                .map(seat -> BigDecimal.valueOf(seat.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(OfferDiscountCode offerCode, List<SeatInventory> seats) {
        return offerService.applyOffer(offerCode, seats);
    }

}
