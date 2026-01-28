package com.pbs.bookingservice.common.ex;

public class BookingNotFoundException extends RuntimeException{
    public BookingNotFoundException(String message) {
        super(message);
    }
    public BookingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
