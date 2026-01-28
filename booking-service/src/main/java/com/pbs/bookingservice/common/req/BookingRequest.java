package com.pbs.bookingservice.common.req;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class BookingRequest {
    private Long userId;
    private String offerCode;
    private Long showId;
    private Integer seatCount;
    List<String> seatNumbers;
}
