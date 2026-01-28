package com.pbs.bookingservice.common.resp;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PricingDetails {
    private  BigDecimal baseAmount;
    private  BigDecimal discountAmount;
    private  BigDecimal finalAmount;

}
