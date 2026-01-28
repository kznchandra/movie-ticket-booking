package com.pbs.bookingservice.common.req;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter

public class BookingRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    private OfferDiscountCode offerCode;

    @NotNull(message = "Show ID is required")
    private Long showId;

    @NotNull(message = "Seat count is required")
    @Min(value = 1, message = "Seat count must be at least 1")
    @Max(value = 10, message = "Seat count cannot exceed 10")
    private Integer seatCount;

    @NotEmpty(message = "Seat numbers cannot be empty")
    @Size(min = 1, max = 10, message = "Number of seats must be between 1 and 10")
    List<String> seatNumbers;
}
