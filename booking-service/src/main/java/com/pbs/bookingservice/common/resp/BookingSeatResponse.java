package com.pbs.bookingservice.common.resp;

import com.pbs.bookingservice.entity.enums.BookingSeatStatus;

public record BookingSeatResponse(Long bookingSeatId, Long seatInventoryId,  String seatNumber, Double price, BookingSeatStatus status) {
}
