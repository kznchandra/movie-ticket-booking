package com.pbs.bookingservice.common.resp;

import com.pbs.bookingservice.entity.Booking;
import com.pbs.bookingservice.entity.BookingSeat;
import com.pbs.bookingservice.entity.SeatInventory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


public record BookingResponse(Booking booking, List<SeatInventory> seatsInventory, List<BookingSeat> bookingSeats) {
    public  BookingResponse(Booking booking, List<SeatInventory> seatsInventory) {
        this(booking, seatsInventory, booking.getBookingSeats());
    }

}
