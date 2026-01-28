package com.pbs.bookingservice.common.resp;

import com.pbs.bookingservice.entity.Booking;
import com.pbs.bookingservice.entity.BookingSeat;
import com.pbs.bookingservice.entity.SeatInventory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BookingResponse {
    private final Booking booking;
    private final List<SeatInventory> seatsInventory;
    private final List<BookingSeat> seats;

    public static BookingResponse from(Booking booking, List<SeatInventory> seatsInventory) {
        return new BookingResponse(booking, seatsInventory, booking.getBookingSeats());
    }
}
